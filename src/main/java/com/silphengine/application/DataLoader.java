package com.silphengine.application;

import com.silphengine.domain.dto.requests.CardRequest;
import com.silphengine.domain.dto.requests.ExpansionRequest;
import com.silphengine.domain.services.CardService;
import com.silphengine.domain.services.ExpansionService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final ExpansionService expansionService;
    private final CardService cardService;

    @Override
    public void run(String... args) throws Exception  {

        /*
        System.out.println("**** Silph Engine: Loading test data ****");

        try {
            ExpansionRequest expansionRequest = new ExpansionRequest(
                    "sv1",
                    "Scarlet & Violet",
                    "Scarlet & Violet",
                    LocalDate.of(2023, 3, 31),
                    198,
                    "https://images.pokemontcg.io/sv1/logo.png"
            );

            expansionService.createExpansion(expansionRequest);
            System.out.println("Expansion successfully created! -> " + expansionRequest.name());

            CardRequest koraidon = new CardRequest(
                    "sv01-125",
                    "Koraidon ex",
                    "sv1",
                    "Ultra Rare",
                    "POKEMON",
                    List.of("Fighting"),
                    "https://images.pokemontcg.io/sv1/125.png",
                    "G"

            );

            cardService.createCard(koraidon);
            System.out.println("Card successfully created! -> " + koraidon.name());

            CardRequest nestBall = new CardRequest(
                    "sv01-181",
                    "Nest Ball",
                    "sv1",
                    "Uncommon",
                    "TRAINER",
                    null,
                    "https://images.pokemontcg.io/sv1/181.png",
                    "G"
            );

            cardService.createCard(nestBall);
            System.out.println("Trainer Card successfully created! -> " + nestBall.name());

        } catch (Exception e) {

            System.out.println("Error: " + e.getMessage());
        }

        System.out.println("**** Silph Engine: End test data load ****");

         */
    }
}
