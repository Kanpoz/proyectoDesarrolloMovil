# 🎨 Paleta de Colores - Referencia Rápida

## Colores Principales

### Primarios (Azul Institucional)
```
primary           #0B63C6  ████████  Azul principal
primary_light     #E9F2FF  ████████  Azul claro
primary_lighter   #E9F0FB  ████████  Azul más claro
primary_dark      #084A9A  ████████  Azul oscuro
```

### Secundarios (Verde)
```
secondary         #1EAD69  ████████  Verde principal
secondary_light   #4CAF50  ████████  Verde claro
secondary_lighter #8BC34A  ████████  Verde lima
```

### Acentos
```
accent_purple       #8E44AD  ████████  Morado
accent_purple_light #9B59B6  ████████  Morado claro
accent_orange       #F5A623  ████████  Naranja
accent_blue         #2196F3  ████████  Azul
accent_blue_light   #4A90E2  ████████  Azul claro
accent_amber        #FFC107  ████████  Ámbar
accent_lime         #8BC34A  ████████  Lima
accent_slate        #607D8B  ████████  Pizarra
accent_green_light  #2ECC71  ████████  Verde claro
```

## Fondos

```
background_main       #F8F9FA  ████████  Fondo principal
background_light      #F5F6FA  ████████  Fondo claro
background_lighter    #F5F5F5  ████████  Fondo más claro
background_docente    #FAF7FF  ████████  Fondo docente (morado claro)
background_card       #F7F3FF  ████████  Fondo de tarjetas
background_card_light #FFFFFF  ████████  Fondo de tarjetas claro
```

## Textos

```
text_primary      #212121  ████████  Texto principal
text_primary_dark #21005D  ████████  Texto principal oscuro
text_secondary    #555555  ████████  Texto secundario
text_tertiary     #757575  ████████  Texto terciario
text_light        #888888  ████████  Texto claro
text_lighter      #777777  ████████  Texto más claro
text_dark         #333333  ████████  Texto oscuro
```

## Grises

```
gray_light  #D9D9D9  ████████  Gris claro
gray_medium #BDBDBD  ████████  Gris medio
gray_dark   #555555  ████████  Gris oscuro
```

## Estados

```
success       #1EAD69  ████████  Éxito (verde)
success_light #4CAF50  ████████  Éxito claro
error         #ff0032  ████████  Error (rojo)
error_light   #E74C3C  ████████  Error claro
warning       #FFC107  ████████  Advertencia (amarillo)
info          #2196F3  ████████  Información (azul)
```

## Base

```
black       #FF000000  ████████  Negro
white       #FFFFFFFF  ████████  Blanco
transparent #00000000  ████████  Transparente
```

---

## Uso por Contexto

### 🏠 Home Estudiante
- Fondo: `background_main`
- Botones: `secondary_light`, `accent_blue`, `accent_amber`, `accent_lime`, `accent_slate`
- Textos: `text_primary`, `text_tertiary`

### 👨‍💼 Home Admin
- Fondo: `background_main`
- Cards: `accent_purple`, `accent_green_light`, `accent_orange`, `accent_blue_light`
- Botones: `secondary`

### 👨‍🏫 Home Docente
- Fondo: `background_docente`
- Cards: `background_card`
- Textos: `text_secondary`, `text_light`, `text_dark`

### 🔐 Login
- Fondo superior: `primary_light` (azulFondo)
- Botón principal: `primary` (AzulInstitucional)
- Botón secundario: `gray_light`

### 📅 Calendario
- Fondo: `primary_lighter`
- Divisores: `gray_medium`, `gray_light`

### ✏️ Editar Curso
- Fondo: `background_light`
- Botón eliminar: `error`
- Botón guardar: `secondary`

---

## Nombres Antiguos (Compatibilidad)

Estos nombres se mantienen para compatibilidad con código existente:

```
AzulInstitucional → primary
azulFondo         → primary_light
verde             → secondary
gris              → gray_light
grisSecundario    → gray_dark
```

**Recomendación**: Usa los nombres nuevos en código nuevo.
