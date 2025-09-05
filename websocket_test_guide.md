# Guide de Test des Notifications WebSocket dans Postman

## 1. Configuration WebSocket dans Postman

### A. Ouvrir un nouvel onglet WebSocket
1. Cliquer sur **"New"** → **"WebSocket Request"**
2. URL : `ws://localhost:8080/ws-notifications`
3. Headers :
   ```
   Authorization: Bearer {{DIRECTION_TOKEN}}
   ```

### B. Connexion WebSocket
1. Cliquer sur **"Connect"**
2. Vérifier que la connexion est établie (statut "Connected")

## 2. Test du Workflow Complet

### Étape 1 : Authentification
```bash
# 1. Créer un utilisateur Direction Technique
POST {{BASE_URL}}/api/auth/Direction_technique/register
{
    "firstname": "Marie",
    "lastname": "Martin", 
    "username": "direction_user",
    "email": "direction@example.com",
    "password": "password123"
}

# 2. Login Direction Technique
POST {{BASE_URL}}/api/auth/login
{
    "username": "direction_user",
    "password": "password123"
}
```

### Étape 2 : Connexion WebSocket
1. Ouvrir un onglet WebSocket
2. URL : `ws://localhost:8080/ws-notifications`
3. Headers : `Authorization: Bearer {{DIRECTION_TOKEN}}`
4. Cliquer sur **"Connect"**

### Étape 3 : Créer une réunion (déclenche la notification)
```bash
# Créer un FTE d'abord
POST {{BASE_URL}}/api/auth/Fte/register
{
    "firstname": "John",
    "lastname": "Doe",
    "username": "fte_user", 
    "email": "fte@example.com",
    "password": "password123"
}

# Login FTE
POST {{BASE_URL}}/api/auth/login
{
    "username": "fte_user",
    "password": "password123"
}

# Créer une réunion (déclenche la notification)
POST {{BASE_URL}}/api/reunions/creerReunion
Authorization: Bearer {{FTE_TOKEN}}
{
    "sujet": "Réunion de test - Validation système",
    "description": "Test des notifications WebSocket", 
    "dateProposee": "2024-12-20T14:00:00",
    "participantIds": []
}
```

### Étape 4 : Vérifier la notification WebSocket
Dans l'onglet WebSocket, tu devrais voir un message comme :
```json
{
    "id": 1,
    "title": "Nouvelle réunion à valider : Réunion de test - Validation système",
    "message": "Une nouvelle réunion a été créée par fte_user.\n\nSujet : Réunion de test - Validation système\nDate proposée : 20/12/2024 14:00\n\nVeuillez examiner et valider cette réunion.",
    "createdAt": "2024-12-19T10:30:00",
    "read": false
}
```

## 3. Tests des Différentes Notifications

### A. Notification de création de réunion
- **Déclencheur** : FTE crée une réunion
- **Destinataire** : Toutes les Directions Techniques
- **Contenu** : Détails de la réunion à valider

### B. Notification de validation
- **Déclencheur** : Direction Technique valide une réunion
- **Destinataire** : FTE créateur + Participants Commission Technique
- **Contenu** : Confirmation de validation

### C. Notification de rejet
- **Déclencheur** : Direction Technique rejette une réunion
- **Destinataire** : FTE créateur
- **Contenu** : Motif du rejet

### D. Notification de confirmation finale
- **Déclencheur** : FTE confirme une réunion validée
- **Destinataire** : Tous les acteurs
- **Contenu** : Confirmation finale

## 4. Scripts de Test Automatisés

### A. Test de notification de création
```javascript
// Dans Postman Tests
pm.test("Notification WebSocket reçue", function () {
    // Vérifier que la notification a été reçue
    const response = pm.response.json();
    pm.expect(response.title).to.include("Nouvelle réunion à valider");
    pm.expect(response.message).to.include("Une nouvelle réunion a été créée");
});
```

### B. Test de workflow complet
```javascript
// Test du workflow complet
pm.test("Workflow complet fonctionne", function () {
    // 1. Créer réunion
    // 2. Vérifier notification Direction
    // 3. Valider réunion
    // 4. Vérifier notification FTE
    // 5. Confirmer réunion
    // 6. Vérifier notification finale
});
```

## 5. Dépannage

### Problème : WebSocket ne se connecte pas
- Vérifier que le serveur Spring Boot est démarré
- Vérifier l'URL : `ws://localhost:8080/ws-notifications`
- Vérifier le token JWT dans les headers

### Problème : Pas de notification reçue
- Vérifier que l'utilisateur Direction Technique existe
- Vérifier que le token est valide
- Vérifier les logs Spring Boot pour les erreurs

### Problème : Notification mal formatée
- Vérifier la structure du DTO NotificationDTO
- Vérifier la sérialisation JSON

## 6. Logs à Surveiller

Dans les logs Spring Boot, tu devrais voir :
```
INFO  - Notification envoyée à direction_user
INFO  - WebSocket message sent to /queue/notifications
INFO  - Réunion créée avec succès
```

## 7. Variables d'Environnement Postman

```json
{
    "BASE_URL": "http://localhost:8080",
    "FTE_TOKEN": "",
    "DIRECTION_TOKEN": "",
    "COMMISSION_TOKEN": ""
}
```

## 8. Collection Postman Import

1. Importer le fichier `postman_collection.json`
2. Configurer les variables d'environnement
3. Exécuter les tests dans l'ordre
4. Ouvrir un onglet WebSocket séparé pour surveiller les notifications 