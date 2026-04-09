<div align="center">
  <img src="https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/items/poke-ball.png" width="80" height="80" alt="SilphEngine Logo">
  <h1>⚡ SilphEngine</h1>
  <p><strong>The High-Performance Logic Engine for TCG Professionals</strong></p>

  [![Java Version](https://img.shields.io/badge/Java-25-orange?style=for-the-badge&logo=openjdk)](https://openjdk.org/)
  [![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.0-brightgreen?style=for-the-badge&logo=springboot)](https://spring.io/projects/spring-boot)
  [![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16+-blue?style=for-the-badge&logo=postgresql)](https://www.postgresql.org/)
  [![License](https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge)](https://opensource.org/licenses/MIT)
</div>

---

### 📖 Overview

**SilphEngine** is not just another card tracker. It is a backend laboratory engineered to solve the complex logic of **Pokémon TCG**. By decoupling physical ownership from strategic deck building, it provides a robust framework for collection valuation and competitive legality checks.

Built with **Spring Boot 4**, it leverages the latest JVM features like **Virtual Threads** to handle massive data ingestion from external APIs without breaking a sweat.

---

### 🏛️ Architecture Highlights

To achieve a "Senior-Grade" codebase, we implemented the following architectural patterns:

* **🛡️ Surrogate-Business Key Pattern:** Decoupled internal logic from the TCGdex API using `UUID` for internal persistence and `externalId` for API synchronization.
* **📦 Smart Inventory Stacking:** Optimized database footprint by grouping assets by `Condition` and `Finish` using custom Enums.
* **🧩 Domain Isolation:** Separate entity domains for the Global Catalog, User Inventory, and Deck Strategy.

---

### 📊 System Design

```mermaid
classDiagram
    User: -UUID id
    User: -String nickname
    User: -String email
    User: -String password
    User: -LocalDateTime createdAt
    User: -List~InventoryCard~ collection
    User: -List~Deck~ decks

    Card: -UUID id
    Card: -String externalId
    Card: -String name
    Card: -String imageUrl
    Card: -List~String~ types
    Card: -Expansion expansion
    Card: -String rarity
    Card: -String category

    Expansion: -UUID id
    Expansion: -String externalId
    Expansion: -String name
    Expansion: -String serieName
    Expansion: -LocalDate releaseDate
    Expansion: -int totalCards
    Expansion: -String logoUrl

    InventoryCard: -UUID id
    InventoryCard: -User owner
    InventoryCard: -Card card
    InventoryCard: -Integer quantity
    InventoryCard: -Condition condition
    InventoryCard: -CardFinish cardFinish

    Deck: -UUID id
    Deck: -String name
    Deck: -User owner
    Deck: -Boolean isLegal

    DeckCard: -UUID id
    DeckCard: -Card card
    DeckCard: -Deck deck
    DeckCard: -Integer quantity
```
