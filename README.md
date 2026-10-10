# PC01 · Lotería en tiempo real (DSM 2026-02)

Aplicación Android (Kotlin + Jetpack Compose + Firebase Authentication + Cloud Firestore) para registrar una jugada de 6 números y seguir un sorteo en tiempo real.

## Integrantes

| Código | Apellido paterno | Nombres |
|---|---|---|
| 22200415 | Pariacuri | _(completar)_ |
| 22102018 | Albornoz | _(completar)_ |

## Requisitos

- Android Studio (Ladybug o superior) con JDK 17
- Un proyecto de Firebase con **Authentication (correo/contraseña)** y **Cloud Firestore** habilitados
- Dos emuladores o dispositivos (para la demostración)

## Configuración

1. En la consola de Firebase, agrega una app Android con el paquete `pe.edu.esan.loteria`.
2. Descarga `google-services.json` y colócalo en `app/google-services.json`.
3. Authentication → Método de acceso → habilita **Correo electrónico/contraseña**.
4. Firestore → crea la base de datos y pega en **Reglas** el contenido de `firestore.rules`.
5. Abre el proyecto en Android Studio, haz **Sync Gradle** y ejecuta la configuración `app`.

## Estructura en Firestore

```
sorteos/{sorteoId}                    nombre, fecha, estado (abierto | en_curso | finalizado)
sorteos/{sorteoId}/bolillas/{autoId}  numero (number), orden (number 1..6)
sorteos/{sorteoId}/jugadas/{uid}      sorteoId, uid, email, numeros (array de 6 numbers), registradoEn
```

- El sorteo usado es `sorteos/sorteo-prueba`. Si no existe, la app lo crea al entrar a la pantalla de jugada con estado `abierto`.
- La jugada usa el **UID como id del documento** → una sola jugada por usuario y sorteo.
- La relación bolilla/jugada → sorteo queda establecida por la ubicación (subcolecciones) y además la jugada guarda `sorteoId`.
- `GANADOR` / `SIGUE INTENTANDO` **no se guardan**: la app los calcula.

## Arquitectura

```
app/src/main/java/pe/edu/esan/loteria/
├── data/          AuthRepository (Firebase Auth) · LoteriaRepository (Firestore + listeners)
├── domain/        Modelos · LoteriaCalculo (validaciones, aciertos, resultado — lógica pura)
└── ui/            auth · jugada · seguimiento · common · navigation · theme (MVVM con StateFlow)
```

- **Autenticación:** `AuthRepository` usa `createUserWithEmailAndPassword` / `signInWithEmailAndPassword`; los errores se traducen a mensajes en español. La sesión persiste: al reabrir la app se entra directo a la jugada.
- **Persistencia:** `registrarJugada` usa una **transacción** que lee el estado del sorteo y solo escribe si está `abierto`. Las reglas de Firestore refuerzan lo mismo en el servidor.
- **Escucha de cambios:** cada `addSnapshotListener` está envuelto en un `callbackFlow` con `awaitClose { registration.remove() }`. Los ViewModels combinan los flujos con `combine(...).stateIn(viewModelScope, WhileSubscribed(5000))`: hay **un solo listener por recurso** (no duplicados) y se elimina al salir de la pantalla. En Logcat (filtro `LoteriaListener`) se ve `ON`/`OFF` de cada listener.
- **Cálculos:** `LoteriaCalculo` (sin dependencias de Android):
  - Bolillas publicadas = bolillas válidas recibidas (0..6), ordenadas por `orden`.
  - Aciertos = |números de la jugada ∩ números sorteados| (el orden no importa).
  - Resultado: `abierto` → mensaje de espera; `en_curso` → "Sorteo en curso"; `finalizado` con 6 bolillas → GANADOR (6 aciertos) o SIGUE INTENTANDO; `finalizado` con menos de 6 → "Esperando resultados".
- **Pruebas unitarias:** `app/src/test/.../LoteriaCalculoTest.kt` (validaciones, aciertos y la demostración obligatoria). Ejecutar: `./gradlew test`.

## Demostración obligatoria

| Usuario | Jugada | Esperado |
|---|---|---|
| A | 5, 12, 18, 23, 34, 41 | 6 aciertos · GANADOR |
| B | 5, 12, 18, 23, 34, 42 | 5 aciertos · SIGUE INTENTANDO |

1. Registrar las cuentas A y B (una en cada emulador) y guardar sus jugadas con el sorteo `abierto`. Abrir **Ver seguimiento** en ambos.
2. Consola → `sorteos/sorteo-prueba` → cambiar `estado` a `en_curso` (ambas pantallas muestran "Sorteo en curso").
3. En `sorteos/sorteo-prueba/bolillas` agregar **un documento por bolilla** (ID automático), campos `numero` (number) y `orden` (number): 5/1, 12/2, 18/3, 23/4, 34/5, 41/6, esperando que aparezca en ambas pantallas antes de la siguiente.
4. Tras la 5.ª bolilla ambos tienen 5 aciertos y no hay resultado definitivo.
5. Tras la 6.ª, cambiar `estado` a `finalizado` → A: **GANADOR**, B: **SIGUE INTENTANDO**.
6. Cerrar y volver a abrir la app en una sesión: se recuperan jugada, bolillas y se recalcula el resultado.

> Para repetir la prueba: borrar la subcolección `bolillas` y las `jugadas`, y volver a poner `estado = abierto`.
