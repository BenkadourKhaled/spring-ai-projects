# Spring AI Fast Track — Journal d'exercices

Journal des exercices réalisés dans le cadre du cours **Spring AI Fast Track**.

**Stack technique utilisée**
- Java 27
- Spring Boot 4.1
- Spring AI 2.0.1
- Maven
- Docker Model Runner
- Ollama
- Gemini

> Ce README documente uniquement les exercices réalisés et les informations effectivement vérifiées. Les exercices non encore réalisés, les pistes de test et les mesures non collectées ne sont pas présentés comme des travaux terminés.

---

## Série 01 — Say Hello to AI in Spring Boot

**Dossier :** `series-01/`

### Exercices réalisés et documentés

| Exercice | Réalisation |
|---|---|
| 1.1 | Projet généré avec Spring Initializr. |
| 1.3 | Création de l'endpoint `GET /api/chat?message=...` avec `ChatClient`. |
| 1.4 | Affichage des beans Spring AI au démarrage avec `BeanLister` et activation des logs `debug=true`. |
| 1.5 | Estimation du coût de 10 000 requêtes courtes et longues. |
| 1.6 | Comparaison de l'utilisation de `ChatModel` + `Prompt` avec `ChatClient`. |
| 1.7 | Utilisation d'Ollama comme fournisseur local sans modifier le contrôleur. |
| 1.8 | Intégration de Docker Model Runner avec le starter OpenAI, configuration du `base-url` et utilisation d'une clé factice. |

*Remarque : l'exercice 1.2 n'est pas documenté dans cette version, faute d'informations suffisantes. Il n'est donc pas marqué comme réalisé.*

---

### Exercice 1.7 — Passer en local avec Ollama

**Objectif :** utiliser un modèle d'IA local avec Spring AI sans modifier le contrôleur.

#### Configuration utilisée

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

#### Lancement

```bash
ollama pull llama3.2:1b
./mvnw spring-boot:run
```

#### Test

```text
http://localhost:8080/api/chat?message=Bonjour
```

#### Erreurs rencontrées

Aucune erreur précise liée à cet exercice n'a été consignée dans les informations disponibles. Aucune erreur n'est ajoutée artificiellement.

---

### Exercice 1.8 — Docker Model Runner

**Objectif :** lancer un modèle local avec Docker Model Runner et utiliser le starter OpenAI de Spring AI pour communiquer avec lui via une API compatible OpenAI.

#### Architecture utilisée

```text
Client HTTP
    |
    v
Spring Boot Application
    |
    v
Spring AI ChatClient
    |
    v
OpenAI Chat Model (client compatible OpenAI)
    |
    v
Docker Model Runner
    |
    v
Modèle local : smollm2
```

Docker Model Runner exécute le modèle localement et expose une API compatible avec le format OpenAI. Spring AI peut donc utiliser le starter OpenAI en redirigeant son client vers cette API locale.

#### Configuration utilisée

Dans `application.yml` :

```yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY:dummy-key}
      base-url: http://localhost:12434/engines/v1
      chat:
        options:
          model: ai/smollm2
```

Le nom du modèle doit correspondre exactement à l'identifiant disponible dans Docker Model Runner.

#### Erreur rencontrée : HTTP 404

**Symptôme :** le premier appel effectué depuis Spring AI retournait une erreur HTTP 404.

**Cause identifiée :** le client OpenAI construit les endpoints à partir du `base-url` configuré. Dans l'environnement utilisé, l'API compatible OpenAI de Docker Model Runner était exposée sous le chemin `/engines/v1`. Un `base-url` ne contenant pas le bon chemin conduisait donc à une URL incorrecte.

**Correction appliquée :**

```yaml
base-url: http://localhost:12434/engines/v1
```

**Méthode de diagnostic :**
1. Vérifier que Docker Model Runner est démarré.
2. Vérifier que le modèle attendu est disponible.
3. Tester l'endpoint de l'API locale avec `curl`.
4. Reporter le bon chemin dans `base-url`.
5. Relancer l'application Spring Boot et refaire l'appel.

Cette démarche permet de distinguer un problème d'accès à l'API locale d'un problème de configuration dans l'application Spring.

#### Pourquoi une clé API est-elle demandée avec un modèle local ?

Le starter OpenAI de Spring AI utilise un client compatible avec l'API OpenAI. Ce client attend une valeur pour le paramètre `api-key`, même lorsque son URL de base est redirigée vers un serveur local compatible.

Dans cet exercice, `dummy-key` est une valeur factice destinée à satisfaire la configuration du client. Ce n'est pas une véritable clé OpenAI.

Il faut distinguer trois éléments :

- **Configuration du client :** le client attend une valeur pour son paramètre `api-key`.
- **Exécution du modèle :** Docker Model Runner exécute le modèle sur la machine locale.
- **Authentification du serveur :** une clé factice ne remplace pas une authentification réelle si le serveur local en exige une.

#### Comparaison effectuée : Gemini et `smollm2`

La même question a été posée à Gemini et au modèle local `smollm2`.

**Observation pendant les tests :** Gemini a pris un peu plus de temps pour répondre que le modèle léger exécuté localement avec Docker Model Runner. Dans cet environnement de test, `smollm2` a donc répondu plus rapidement.

Cette observation porte uniquement sur la vitesse constatée pendant ces essais. Elle ne permet pas de conclure que le modèle local fournit de meilleures réponses. La qualité doit être évaluée séparément avec les mêmes questions et des critères identiques.

| Critère | Gemini | Docker Model Runner (`smollm2`) |
|---|---|---|
| Hébergement | Cloud | Local |
| Connexion Internet | Nécessaire pour accéder à l'API | Pas nécessaire pour l'inférence locale |
| Temps de réponse observé | Un peu plus long pendant mes tests | Plus rapide pendant mes tests |
| Ressources d'exécution | Infrastructure du fournisseur | Ressources de la machine locale |
| Qualité des réponses | Non évaluée de manière chiffrée | Non évaluée de manière chiffrée |

---

## Bilan des erreurs réellement documentées

| Exercice | Erreur | Cause | Correction |
|---|---|---|---|
| 1.8 | HTTP 404 lors de l'appel à Docker Model Runner | Chemin incorrect ou incomplet dans le `base-url` | Utiliser `http://localhost:12434/engines/v1` dans la configuration testée. |

Les autres exercices ne comportent pas d'erreur précise consignée dans les informations disponibles. Pour éviter d'inventer des incidents, seules les erreurs réellement signalées sont inscrites ici.

---

## Principaux acquis

- Utiliser `ChatClient` pour appeler un modèle depuis une application Spring Boot.
- Comprendre la différence entre `ChatModel` + `Prompt` et `ChatClient`.
- Connecter Spring AI à un modèle local avec Ollama.
- Connecter Spring AI à Docker Model Runner via le starter OpenAI.
- Configurer un `base-url` pour cibler une API compatible OpenAI.
- Comprendre pourquoi le client peut demander une valeur `api-key`, même avec un modèle local.
- Diagnostiquer une erreur HTTP 404 liée à un endpoint API incorrect.
- Distinguer la vitesse de réponse de la qualité des réponses.
