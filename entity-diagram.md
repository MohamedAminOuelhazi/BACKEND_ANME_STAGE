# Entity Relationship Diagram

```plantuml
@startuml
' Entities with their fields

class user {
  long id
  String firstname
  String lastname
  String username
  String email
  String password
  String usertype
}

class Commission_techniques {
}

class Direction_technique {
  String departement
}

class Fte {
}

class Notification {
  Long id
  String title
  String message
  boolean read
  LocalDateTime createdAt
}

class Pv {
  Long id
  String titre
  String contenu
  PVStatus status
}

class PVStatus {
  PENDING
  ACCEPTED
  REJECTED
}

class PVVersion {
  UUID id
  LocalDateTime dateCreation
  String cheminFichier
  int version
}

class Reunion {
  Long id
  String sujet
  String description
  LocalDateTime dateProposee
  ReunionStatus status
}

class ReunionDocument {
  Long id
  String nomFichier
  String cheminFichier
  LocalDateTime dateUpload
}

class ReunionStatus {
  PENDING
  SCHEDULED
  VALIDATED
  REJECTED
  CANCELLED
}

class Signature {
  Long id
  boolean accepte
  String commentaire
  LocalDateTime dateSignature
  String cheminSignature
}

' Inheritance relationships
Commission_techniques --|> user
Direction_technique --|> user
Fte --|> user

' JPA Relationships
user "1" -- "*" Notification : receives
Pv "1" -- "*" PVVersion : has
Pv "1" -- "*" Signature : signed
Reunion "1" -- "*" Pv : generates
Reunion "1" -- "*" ReunionDocument : has
Fte "1" -- "*" Reunion : creates
Reunion "1" -- "*" Direction_technique : validated by
Reunion "1" -- "*" Commission_techniques : participated by
user "1" -- "*" Signature : signs

@enduml