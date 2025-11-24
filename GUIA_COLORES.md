# 🎨 Sistema de Colores - Guía de Uso

## 📋 Resumen de Cambios

Se ha simplificado el sistema de colores a una **Paleta de 3 Colores de Marca** genéricos para garantizar consistencia visual y flexibilidad ante cambios institucionales. Además, se ha implementado soporte completo para **Dark Mode**.

---

## ✅ Paleta de 3 Colores de Marca (Generic Brand Colors)

Toda la aplicación se basa ahora en 3 tonos principales definidos como "Brand Colors". Esto permite cambiar el color de toda la institución (ej. de azul a rojo) modificando solo 3 líneas.

### 1. **Brand Primary** (`brand_primary`) - #2563EB (Default: Azul)
- **Uso**: Botones principales, barras de navegación, elementos destacados.
- **Equivalente Dark Mode**: Se usa un tono más claro (`brand_tertiary`) para mejor contraste.

### 2. **Brand Secondary** (`brand_secondary`) - #1E40AF (Default: Azul Oscuro)
- **Uso**: Elementos de alto contraste, textos oscuros, estados activos.
- **Equivalente Dark Mode**: Se usa como fondo o acento sutil.

### 3. **Brand Tertiary** (`brand_tertiary`) - #60A5FA (Default: Azul Claro)
- **Uso**: Detalles, fondos suaves, estados secundarios.
- **Equivalente Dark Mode**: Se convierte en el color primario vibrante.

---

## 🌙 Soporte Dark Mode

El sistema ahora detecta automáticamente el tema del dispositivo y ajusta los colores:

| Elemento | Light Mode | Dark Mode |
|----------|------------|-----------|
| **Fondo** | `background_main` (#F8FAFC) | `background_main` (#0F172A) |
| **Texto** | `text_primary` (#0F172A) | `text_primary` (#F8FAFC) |
| **Tarjetas** | `background_card` (#FFFFFF) | `background_card` (#1E293B) |
| **Primario** | `brand_primary` | `brand_tertiary` |

---

## 📖 Cómo Usar la Paleta

Siempre usa los nombres semánticos, **NUNCA** los nombres de marca (`brand_...`) directamente en los layouts, para que el Dark Mode funcione.

### ✅ Correcto (Soporta Dark Mode):
```xml
<TextView
    android:textColor="@color/text_primary"
    android:background="@color/background_card"
    app:backgroundTint="@color/primary" />
```

### ❌ Incorrecto (No cambia en Dark Mode):
```xml
<TextView
    android:textColor="#0F172A"
    android:background="@color/brand_primary" />
```

---

## 🔄 Mapeo de Colores Antiguos

Para mantener compatibilidad, los colores antiguos han sido reasignados a la nueva paleta genérica:

- `accent_purple` -> **Brand Primary**
- `accent_orange` -> **Brand Secondary**
- `accent_green` -> **Brand Primary**
- `verde`, `rojo`, etc. -> Se mantienen para estados de éxito/error.

---

**Fecha de actualización**: 2025-11-24
**Versión**: 2.1 (Generic Brand Palette + Dark Mode)
