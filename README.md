# DineUp (SWE Project)

Applicazione Java console per la gestione di:
- autenticazione utenti (customer/staff/owner),
- menu e piatti,
- prenotazioni tavoli,
- ordini e notifiche.

## Requisiti

- Java 17+ (consigliato)
- PostgreSQL 14+
- Driver/librerie già presenti in `lib/`:
    - `postgresql-42.7.8.jar`
    - `jbcrypt-0.4.jar`

## Configurazione database

L'app legge la configurazione DB da:
1. variabili ambiente (priorità alta):
    - `DB_URL`
    - `DB_USER`
    - `DB_PASSWORD`
2. file locale `src/ORM/db.properties` (fallback).

### Opzione A — variabili ambiente

```bash
export DB_URL="jdbc:postgresql://localhost:5432/dineup"
export DB_USER="postgres"
export DB_PASSWORD="postgres"
```

### Opzione B — file `db.properties`

Crea `src/ORM/db.properties` con:

```properties
db.URL=jdbc:postgresql://localhost:5432/dineup
db.USER=postgres
db.PASSWORD=postgres
```

## Inizializzazione schema e dati

Da `psql` esegui:

```sql
\i sql/schema.sql
\i sql/seed.sql
```

## Compilazione

```bash
javac -cp "lib/postgresql-42.7.8.jar:lib/jbcrypt-0.4.jar" -d out $(find src -name '*.java')
```

## Esecuzione

```bash
java -cp "out:lib/postgresql-42.7.8.jar:lib/jbcrypt-0.4.jar" Main
```

## Note utili

- La seed contiene password placeholder (`*_hashed_pwd`): per test login reali è necessario usare hash BCrypt validi.
- Gli stati seed sono allineati agli enum Java correnti (`ReservationStatus`, `OrderStatus`, `PaymentMethod`, `TypeNotification`, `StatusNotification`).

## Struttura progetto (high-level)

- `src/CLI`: interfaccia testuale
- `src/Controller`: orchestrazione use case per ruolo
- `src/ServiceLayer`: logica applicativa
- `src/ORM`: accesso ai dati (DAO)
- `src/DomainModel`: entità e value object
- `sql/`: schema e seed PostgreSQL