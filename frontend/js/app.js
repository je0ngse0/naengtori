const API = '/api';
const AUTH_API = API + '/auth/me';
const CSRF_API = API + '/auth/csrf';
const LOGOUT_API = API + '/auth/logout';
const $ = selector => document.querySelector(selector);

const loginPage = $('#login-panel');
const homePage = $('#home-panel');
const discoverPage = $('#discover-panel');
const profileButton = $('#open-profile');
const profilePanel = $('#profile-panel');
const profileEditPanel = $('#profile-edit-panel');
const fridge = $('#fridge-panel');
const recipes = $('#recipe-panel');
const list = $('#ingredient-list');
const form = $('#ingredient-form');
const input = $('#ingredient-input');
const suggestions = $('#suggestions');
const modal = $('#delete-modal');
const toast = $('#gulp-toast');

const ingredientCatalog = [
    '소시지', '소주', '산삼', '새우', '시금치', '사과', '두부', '대파',
    '돼지고기', '간장', '버섯', '감자', '오이', '우유', '계란', '밥',
    '김치', '양파', '토마토'
];

let currentMember = null;
let csrf = null;
let ingredients = [];
let recommendations = [];
let pending = null;
let toastTimer = null;
let recipeReturnPage = 'my';

function closePanels() {
    [profilePanel, profileEditPanel, fridge, recipes].forEach(panel => {
        panel.hidden = true;
    });
    document.body.classList.remove('has-modal');
}

function showLogin() {
    currentMember = null;
    csrf = null;
    closePanels();
    loginPage.hidden = false;
    homePage.hidden = true;
    discoverPage.hidden = true;
    profileButton.hidden = true;
    setActiveNav('my');
}

function showHome() {
    closePanels();
    loginPage.hidden = true;
    homePage.hidden = false;
    discoverPage.hidden = true;
    profileButton.hidden = false;
    setActiveNav('my');
}

function showDiscover() {
    closePanels();
    loginPage.hidden = true;
    homePage.hidden = true;
    discoverPage.hidden = false;
    profileButton.hidden = currentMember === null;
    setActiveNav('popular');
}

function openPanel(panel) {
    closePanels();
    panel.hidden = false;
    if (panel === recipes) {
        document.body.classList.add('has-modal');
    }
}

function setActiveNav(active) {
    $('#nav-my-naengtori').classList.toggle('is-active', active === 'my');
    $('#nav-popular-recipes').classList.toggle('is-active', active === 'popular');
}

function openMyNaengtori() {
    if (currentMember) {
        showHome();
    } else {
        showLogin();
    }
}

function showToast(message) {
    clearTimeout(toastTimer);
    toast.textContent = message;
    toast.hidden = false;
    toastTimer = setTimeout(() => {
        toast.hidden = true;
    }, 1800);
}

function setImage(image, url) {
    if (url) {
        image.src = url;
    } else {
        image.removeAttribute('src');
    }
}

function normalizeMember(member) {
    return {
        nickname: member.nickname || '냉토리',
        email: member.email || '',
        provider: (member.provider || '').toUpperCase(),
        profileImageUrl: member.profileImageUrl || ''
    };
}

function applyMember(member) {
    currentMember = normalizeMember(member);
    const providerName = currentMember.provider || '-';
    const images = [
        $('#header-profile-image'),
        $('#profile-image'),
        $('#profile-image-preview')
    ];

    $('#header-nickname').textContent = currentMember.nickname + '님';
    $('#home-nickname').textContent = currentMember.nickname;
    $('#profile-nickname').textContent = currentMember.nickname;
    $('#profile-email').textContent = currentMember.email || '제공되지 않음';
    $('#profile-provider').textContent = providerName;
    $('#edit-provider').textContent = providerName;
    $('#nickname-input').value = currentMember.nickname;
    $('#email-input').value = currentMember.email;

    [$('#profile-provider'), $('#edit-provider')].forEach(badge => {
        badge.classList.toggle('naver', providerName === 'NAVER');
    });

    images.forEach(image => setImage(image, currentMember.profileImageUrl));
}

async function loadCsrfToken() {
    const response = await fetch(CSRF_API, {
        headers: { Accept: 'application/json' }
    });

    if (!response.ok) {
        throw new Error('CSRF 토큰을 불러오지 못했습니다.');
    }

    csrf = await response.json();
}

async function csrfHeaders(headers = {}) {
    if (!csrf) {
        await loadCsrfToken();
    }

    return {
        ...headers,
        [csrf.headerName]: csrf.token
    };
}

async function loadCurrentMember() {
    try {
        const response = await fetch(AUTH_API, {
            headers: { Accept: 'application/json' }
        });

        if (response.status === 401 || response.status === 403) {
            showLogin();
            return;
        }

        if (!response.ok) {
            throw new Error('회원 정보를 불러오지 못했습니다.');
        }

        applyMember(await response.json());
        showHome();

        try {
            await loadCsrfToken();
        } catch (error) {
            showToast('보안 토큰을 불러오지 못했어요.');
        }

        await loadIngredients();
    } catch (error) {
        // OAuth 백엔드가 아직 연결되지 않은 동안에는 로그인 화면을 보여줍니다.
        showLogin();
    }
}

$('#brand-home').addEventListener('click', event => {
    event.preventDefault();
    openMyNaengtori();
});

$('#nav-my-naengtori').addEventListener('click', openMyNaengtori);

$('#nav-popular-recipes').addEventListener('click', async () => {
    showDiscover();
    await loadPublicRecipes();
});

$('#recipe-search-form').addEventListener('submit', async event => {
    event.preventDefault();
    const ingredient = $('#recipe-search-input').value.trim();

    if (!ingredient) {
        $('#recipe-search-input').focus();
        return;
    }

    showDiscover();
    await searchPublicRecipes(ingredient);
});

profileButton.addEventListener('click', () => {
    applyMember(currentMember);
    openPanel(profilePanel);
});

$('#profile-panel .close-panel').addEventListener('click', showHome);
$('#profile-edit-panel .close-panel').addEventListener('click', () => openPanel(profilePanel));

$('#open-profile-edit').addEventListener('click', () => {
    applyMember(currentMember);
    $('#profile-save-message').textContent = '';
    openPanel(profileEditPanel);
});

$('#cancel-profile-edit').addEventListener('click', () => {
    applyMember(currentMember);
    openPanel(profilePanel);
});

$('#profile-edit-form').addEventListener('submit', async event => {
    event.preventDefault();

    const nickname = $('#nickname-input').value.trim();
    if (!nickname) {
        $('#profile-save-message').textContent = '닉네임을 입력해 주세요.';
        return;
    }

    try {
        const response = await fetch(AUTH_API, {
            method: 'PATCH',
            headers: await csrfHeaders({
                'Content-Type': 'application/json'
            }),
            body: JSON.stringify({ nickname })
        });

        if (response.status === 401 || response.status === 403) {
            showLogin();
            return;
        }

        if (!response.ok) {
            throw new Error('프로필을 저장하지 못했습니다.');
        }

        applyMember(await response.json());
        $('#profile-save-message').textContent = '';
        showToast('프로필을 저장했어요.');
        openPanel(profilePanel);
    } catch (error) {
        $('#profile-save-message').textContent = '프로필을 저장하지 못했어요.';
    }
});

$('#logout-button').addEventListener('click', async () => {
    try {
        const response = await fetch(LOGOUT_API, {
            method: 'POST',
            headers: await csrfHeaders()
        });
        if (!response.ok) throw new Error();
        showLogin();
    } catch (error) {
        showToast('로그아웃 처리 중 문제가 생겼어요.');
    }
});

$('#open-fridge').addEventListener('click', async () => {
    openPanel(fridge);
    await loadIngredients();
});

$('#open-recipes').addEventListener('click', async () => {
    recipeReturnPage = 'my';
    $('#recipe-panel .panel-header h2').textContent = '오늘의 냉털 레시피';
    recipes.querySelector('.recipe-dialog').classList.remove('is-detail-only');
    $('#recipe-ranking').hidden = false;
    openPanel(recipes);
    await loadRecipes();
});

$('#close-fridge').addEventListener('click', showHome);
$('#close-recipes').addEventListener('click', () => {
    if (recipeReturnPage === 'discover') {
        closePanels();
    } else {
        showHome();
    }
});

recipes.addEventListener('click', event => {
    if (event.target === recipes) {
        $('#close-recipes').click();
    }
});

document.addEventListener('keydown', event => {
    if (event.key === 'Escape' && !recipes.hidden) {
        $('#close-recipes').click();
    }
});

$('#show-ingredient-form').addEventListener('click', () => {
    form.hidden = false;
    input.focus();
});

function choseong(value) {
    const first = 'ㄱㄲㄴㄷㄸㄹㅁㅂㅃㅅㅆㅇㅈㅉㅊㅋㅌㅍㅎ';
    return [...value].map(char => {
        const code = char.charCodeAt(0) - 44032;
        return code >= 0 && code <= 11171 ? first[Math.floor(code / 588)] : char;
    }).join('');
}

function showSuggestions() {
    const query = input.value.trim();
    const found = query
        ? ingredientCatalog.filter(name => name.includes(query) || choseong(name).includes(query)).slice(0, 6)
        : [];

    suggestions.replaceChildren(...found.map(name => {
        const item = document.createElement('li');
        const button = document.createElement('button');
        button.type = 'button';
        button.textContent = name;
        button.addEventListener('click', () => {
            input.value = name;
            suggestions.hidden = true;
        });
        item.append(button);
        return item;
    }));

    suggestions.hidden = found.length === 0;
}

input.addEventListener('input', showSuggestions);

function renderIngredients() {
    list.replaceChildren(...ingredients.map(food => {
        const item = document.createElement('li');
        item.dataset.id = food.id;
        item.dataset.food = food.name;

        const name = document.createElement('button');
        name.className = 'ingredient-name';
        name.type = 'button';
        name.textContent = food.name;
        name.addEventListener('click', () => {
            list.querySelectorAll('li').forEach(other => {
                if (other !== item) other.classList.remove('is-editing');
            });
            item.classList.toggle('is-editing');
        });

        const remove = document.createElement('button');
        remove.className = 'delete-food';
        remove.type = 'button';
        remove.textContent = '×';
        remove.ariaLabel = food.name + ' 삭제';
        remove.addEventListener('click', () => {
            pending = food;
            $('#delete-food-name').textContent = food.name;
            modal.hidden = false;
            $('#cancel-delete').focus();
        });

        item.append(name, remove);
        return item;
    }));

    const names = ingredients.map(item => item.name);
    $('#ingredient-count').textContent = names.length;
    $('#home-ingredient-count').textContent = names.length;
    $('#ingredient-preview').textContent = names.length
        ? names.join(' · ')
        : '아직 냉장고에 등록된 재료가 없어요.';
}

async function loadIngredients() {
    try {
        const response = await fetch(API + '/ingredients');
        if (response.status === 401 || response.status === 403) {
            showLogin();
            return;
        }
        if (!response.ok) throw new Error();
        ingredients = await response.json();
        renderIngredients();
    } catch (error) {
        showToast('식재료를 불러오지 못했어요.');
    }
}

form.addEventListener('submit', async event => {
    event.preventDefault();
    const name = input.value.trim();

    if (!name || ingredients.some(item => item.name === name)) return;

    try {
        const response = await fetch(API + '/ingredients', {
            method: 'POST',
            headers: await csrfHeaders({
                'Content-Type': 'application/json'
            }),
            body: JSON.stringify({ name })
        });

        if (response.status === 401 || response.status === 403) {
            showLogin();
            return;
        }
        if (!response.ok) throw new Error();

        ingredients.push(await response.json());
        input.value = '';
        suggestions.hidden = true;
        renderIngredients();
    } catch (error) {
        showToast('식재료를 추가하지 못했어요.');
    }
});

$('#cancel-delete').addEventListener('click', () => {
    modal.hidden = true;
    pending = null;
});

$('#confirm-delete').addEventListener('click', async () => {
    if (!pending) return;

    try {
        const response = await fetch(API + '/ingredients/' + pending.id, {
            method: 'DELETE',
            headers: await csrfHeaders()
        });

        if (response.status === 401 || response.status === 403) {
            modal.hidden = true;
            showLogin();
            return;
        }
        if (!response.ok) throw new Error();

        ingredients = ingredients.filter(item => item.id !== pending.id);
        pending = null;
        modal.hidden = true;
        renderIngredients();
        gulp();
        showToast('꿀꺽! 냉장고에서 사라졌어요.');
    } catch (error) {
        showToast('식재료를 삭제하지 못했어요.');
    }
});

function gulp() {
    try {
        const Context = window.AudioContext || window.webkitAudioContext;
        const context = new Context();
        const oscillator = context.createOscillator();
        const gain = context.createGain();

        oscillator.frequency.setValueAtTime(260, context.currentTime);
        oscillator.frequency.exponentialRampToValueAtTime(85, context.currentTime + .28);
        gain.gain.setValueAtTime(.0001, context.currentTime);
        gain.gain.exponentialRampToValueAtTime(.16, context.currentTime + .035);
        gain.gain.exponentialRampToValueAtTime(.0001, context.currentTime + .34);
        oscillator.connect(gain);
        gain.connect(context.destination);
        oscillator.start();
        oscillator.stop(context.currentTime + .35);
    } catch (error) {
        // 소리를 재생할 수 없는 브라우저에서는 조용히 넘어갑니다.
    }
}

async function loadPublicRecipes() {
    $('#discover-eyebrow').textContent = '로그인 없이 둘러보기';
    $('#discover-title').textContent = '지금 인기 있는 레시피';
    $('#discover-description').textContent = '냉토리에서 사람들이 많이 찾는 요리를 만나보세요.';
    await fetchPublicRecipes(
        API + '/recipes/popular',
        '인기 레시피 기능을 준비 중이에요.'
    );
}

async function searchPublicRecipes(ingredient) {
    $('#discover-eyebrow').textContent = '재료로 찾은 레시피';
    $('#discover-title').textContent = '“' + ingredient + '” 검색 결과';
    $('#discover-description').textContent = '“' + ingredient + '”를 활용한 레시피를 모아봤어요.';
    await fetchPublicRecipes(
        API + '/recipes/search?ingredient=' + encodeURIComponent(ingredient),
        '레시피 검색 기능을 준비 중이에요.'
    );
}

async function fetchPublicRecipes(url, unavailableMessage) {
    const status = $('#public-recipe-status');
    const publicList = $('#public-recipe-list');

    publicList.replaceChildren();
    status.textContent = '레시피를 불러오는 중이에요.';

    try {
        const response = await fetch(url, {
            headers: { Accept: 'application/json' }
        });
        if (!response.ok) throw new Error();

        const payload = await response.json();
        const found = Array.isArray(payload) ? payload : (payload.content || []);
        renderPublicRecipes(found);
        status.textContent = found.length
            ? ''
            : '조건에 맞는 레시피를 찾지 못했어요. 다른 재료로 검색해보세요.';
    } catch (error) {
        status.textContent = unavailableMessage;
    }
}

function recipeIcon(name) {
    if (/찌개|국|탕/.test(name)) return '🍲';
    if (/밥|볶음밥|덮밥/.test(name)) return '🍚';
    if (/면|국수|파스타/.test(name)) return '🍝';
    if (/샐러드|무침/.test(name)) return '🥗';
    if (/빵|토스트|샌드위치/.test(name)) return '🥪';
    return '🍽️';
}

function recipeIngredients(recipe) {
    const source = recipe.ingredients || recipe.ingredientNames || [];
    return source.map(item => typeof item === 'string' ? item : item.name).filter(Boolean);
}

function renderPublicRecipes(found) {
    $('#public-recipe-list').replaceChildren(...found.map(recipe => {
        const item = document.createElement('li');
        const button = document.createElement('button');
        const icon = document.createElement('span');
        const name = document.createElement('strong');
        const summary = document.createElement('small');
        const ingredientNames = recipeIngredients(recipe);

        button.className = 'public-recipe-card';
        button.type = 'button';
        icon.className = 'recipe-card-icon';
        icon.textContent = recipeIcon(recipe.name || '');
        name.textContent = recipe.name;
        const viewText = Number.isFinite(recipe.viewCount)
            ? '조회 ' + recipe.viewCount.toLocaleString('ko-KR') + '회'
            : '';
        const ingredientText = ingredientNames.length
            ? ingredientNames.slice(0, 5).join(' · ')
            : (recipe.minutes ? recipe.minutes + '분 레시피' : '레시피 자세히 보기');
        summary.textContent = [viewText, ingredientText].filter(Boolean).join(' · ');

        button.append(icon, name, summary);
        button.addEventListener('click', () => openPublicRecipe(recipe));
        item.append(button);
        return item;
    }));
}

async function openPublicRecipe(recipe) {
    recipeReturnPage = 'discover';
    openPanel(recipes);
    showRecipe(recipe, false);

    try {
        const response = await fetch(API + '/recipes/public/' + recipe.id, {
            headers: { Accept: 'application/json' }
        });
        if (!response.ok) throw new Error();
        showRecipe(await response.json(), false);
    } catch (error) {
        showToast('최신 레시피 정보를 불러오지 못했어요.');
    }
}

async function loadRecipes() {
    try {
        const response = await fetch(API + '/recipes/recommendations');
        if (response.status === 401 || response.status === 403) {
            showLogin();
            return;
        }
        if (!response.ok) throw new Error();
        recommendations = await response.json();
        renderRecipes();
    } catch (error) {
        showToast('레시피를 불러오지 못했어요.');
    }
}

function renderRecipes() {
    const ranking = $('#recipe-ranking');

    ranking.replaceChildren(...recommendations.map(recipe => {
        const item = document.createElement('li');
        const button = document.createElement('button');
        const copy = document.createElement('span');
        const name = document.createElement('strong');
        const info = document.createElement('span');
        const fit = document.createElement('em');
        const missing = recipe.missingIngredients || [];

        button.type = 'button';
        name.textContent = recipe.name;
        info.textContent = '보유 재료 ' + recipe.ownedCount + '/' + recipe.totalCount + ' · ' +
            (missing.length ? '부족: ' + missing.join(', ') : recipe.minutes + '분');
        fit.textContent = missing.length ? missing.length + '개 필요' : '바로 가능';
        fit.className = missing.length ? 'need' : '';
        copy.append(name, info);
        button.append(copy, fit);
        button.addEventListener('click', () => {
            showRecipe(recipe);
            trackRecipeView(recipe.id);
        });
        item.append(button);
        return item;
    }));
}

async function trackRecipeView(recipeId) {
    if (!recipeId) return;

    try {
        await fetch(API + '/recipes/public/' + recipeId, {
            headers: { Accept: 'application/json' }
        });
    } catch (error) {
        // 조회수 기록 실패가 레시피 확인을 방해하지 않게 합니다.
    }
}

function showRecipe(recipe, personalized = true) {
    const detail = $('#recipe-detail');
    const title = document.createElement('h3');
    const summary = document.createElement('p');
    const steps = document.createElement('ol');
    const missing = recipe.missingIngredients || [];
    const ingredientNames = recipeIngredients(recipe);

    recipes.querySelector('.recipe-dialog').classList.toggle('is-detail-only', !personalized);
    $('#recipe-ranking').hidden = !personalized;

    $('#recipe-panel .panel-header h2').textContent = personalized
        ? '오늘의 냉털 레시피'
        : '레시피 상세';
    title.textContent = recipe.name;
    if (personalized) {
        summary.textContent = missing.length
            ? '부족한 재료: ' + missing.join(', ')
            : '현재 재료로 바로 만들 수 있어요.';
    } else {
        summary.textContent = ingredientNames.length
            ? '주요 재료: ' + ingredientNames.join(', ')
            : (recipe.minutes ? '예상 조리 시간: ' + recipe.minutes + '분' : '레시피 정보를 확인해보세요.');
    }

    (recipe.steps || []).forEach(text => {
        const item = document.createElement('li');
        item.textContent = text;
        steps.append(item);
    });

    detail.replaceChildren(title, summary, steps);
}

loadCurrentMember();
