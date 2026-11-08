# Spring AI Fast Track : journal d'exercices

Exercices du cours « Spring AI Fast Track », réalisés série par série.
Stack : Java 27, Spring Boot 4.1, Spring AI 2.0.1, Maven.

## Avancement

| Série | Thème | Statut |
|---|---|---|
| 01 | Say Hello to AI in Spring Boot | en cours (1.1 à 1.7) |
| 02 | Prompts, Advisors et sorties structurées | à faire |
| 03 | Fondations de l'IA générative | à faire |
| 04 | Chat Memory | à faire |
| 05 | RAG | à faire |
| 06 | Tool Calling | à faire |

## Prérequis

- JDK installé, Maven (wrapper `mvnw` fourni)
- [Ollama](https://ollama.com) lancé en local (`http://localhost:11434`)
- Aucune clé API dans le dépôt : les secrets passent par variables d'environnement

---

## Série 01 : Say Hello to AI in Spring Boot

**Dossier :** `series-01/`

### Ce qui a été fait

| Exercice | Résumé |
|---|---|
| 1.1 | Projet généré avec Spring Initializr |
| 1.3 | Endpoint `GET /api/chat?message=...` avec `ChatClient` |
| 1.4 | Beans Spring AI listés au démarrage (`BeanLister`), logs `debug=true` |
| 1.5 | Estimation du coût de 10 000 requêtes (courtes et longues) |
| 1.6 | Comparaison `ChatModel` + `Prompt` contre `ChatClient` |
| 1.7 | Passage du fournisseur cloud à Ollama, sans changer le contrôleur |

### Configuration

```yaml
spring:
  ai:
    model:
      chat: ollama
    ollama:
      chat:
        options:
          model: llama3.2:1b
```

### Lancer

```bash
ollama pull llama3.2:1b
.\mvnw.cmd spring-boot:run
```

Test : `http://localhost:8080/api/chat?message=Bonjour`

### Ce que j'ai appris

- L'auto-configuration crée `ChatModel` et `ChatClient.Builder` ; le `ChatClient` est construit par moi.
- Changer de fournisseur = changer le starter Maven et le YAML, pas le code Java.
- La sortie coûte plus cher que l'entrée : elle domine la facture.

### Erreurs rencontrées

| Erreur | Cause | Correctif |
|---|---|---|
| `Non-parseable POM` | commande `ollama pull` collée dans le `pom.xml` | supprimer la ligne |

---

<!-- Copier le bloc "Série 0X" ci-dessous pour chaque nouvelle série -->
