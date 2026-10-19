package com.naengtori.service;

import com.naengtori.dto.Ingredient;
import com.naengtori.entity.FridgeIngredientEntity;
import com.naengtori.entity.IngredientEntity;
import com.naengtori.entity.MemberEntity;
import com.naengtori.repository.FridgeIngredientRepository;
import com.naengtori.repository.IngredientRepository;
import com.naengtori.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IngredientService {

    private final IngredientRepository ingredientRepository;
    private final FridgeIngredientRepository fridgeRepository;
    private final MemberRepository memberRepository;

    public List<Ingredient> findAll(Long memberId) {
        return fridgeRepository.findAllByMemberIdOrderByIdAsc(memberId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public Ingredient add(Long memberId, String rawName) {
        String name = rawName.trim();

        if (fridgeRepository.existsByMemberIdAndIngredientNameIgnoreCase(
                memberId,
                name
        )) {
            throw new ResponseStatusException(
                    CONFLICT,
                    "이미 냉장고에 등록된 식재료입니다."
            );
        }

        IngredientEntity ingredient =
                ingredientRepository.findByNameIgnoreCase(name)
                        .orElseGet(() ->
                                ingredientRepository.save(
                                        new IngredientEntity(name)
                                )
                        );

        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResponseStatusException(
                        NOT_FOUND,
                        "회원을 찾을 수 없습니다."
                ));

        FridgeIngredientEntity fridgeIngredient =
                fridgeRepository.save(
                        new FridgeIngredientEntity(member, ingredient)
                );

        return toDto(fridgeIngredient);
    }

    @Transactional
    public void delete(Long memberId, long id) {
        FridgeIngredientEntity fridgeIngredient = fridgeRepository
                .findByIdAndMemberId(id, memberId)
                .orElseThrow(() -> new ResponseStatusException(
                    NOT_FOUND,
                    "냉장고에서 식재료를 찾을 수 없습니다."
                ));

        fridgeRepository.delete(fridgeIngredient);
    }

    private Ingredient toDto(
            FridgeIngredientEntity fridgeIngredient
    ) {
        return new Ingredient(
                fridgeIngredient.getId(),
                fridgeIngredient.getIngredient().getName()
        );
    }
}
