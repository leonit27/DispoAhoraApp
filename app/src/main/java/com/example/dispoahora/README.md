# DispoAhora - Conecta en Tiempo Real

**DispoAhora** es una aplicación móvil desarrollada en Android con Jetpack Compose que 
permite a los usuarios gestionar su disponibilidad en tiempo real y visualizar a sus contactos
cercanos sobre un mapa interactivo.

Utiliza tecnología geoespacial para conectar a personas que están libres para realizar actividades 
espontáneas (café, deporte, charlas) basándose en su ubicación actual.

---

## Funcionalidades Principales

* **Estado de Disponibilidad:** Cambia entre "Libre" u "Ocupado" con un solo toque. Al estar libre, puedes definir una actividad específica.
* **Privacidad Inteligente:** Tu ubicación solo se comparte en la base de datos mientras estás en modo "Libre". Al pasar a "Ocupado", tu posición se elimina automáticamente.
* **Búsqueda Geoespacial:** Sección "Cerca de ti" que muestra usuarios disponibles en un radio de 5 km mediante funciones PostGIS.
* **Mapa Interactivo:** Integración con Mapbox para visualizar pines en tiempo real de todos tus contactos seguidos que estén disponibles.
* **Autenticación:** Sistema de registro e inicio de sesión seguro con gestión de perfiles.

## Stack Tecnológico

* **Lenguaje:** Kotlin
* **UI:** Jetpack Compose
* **Base de Datos y Auth:** Supabase (PostgreSQL + GoTrue)
* **Motor Geoespacial:** PostGIS
* **Mapas:** Mapbox SDK para Android

## Capturas de pantalla

### Acceso y Perfil
![Login](imagenes/Login.jpg)
![Register](imagenes/Register.jpg)
![Perfil](imagenes/Perfil.jpg)
![Perfil2](imagenes/Perfil2.jpg)

### Estado y Actividad
![PantallaPrincipal](imagenes/PantallaPrincipal.jpg)
![EstadoLibre](imagenes/EstadoLibre.jpg)
![EstadoOcupado](imagenes/EstadoOcupado.jpg)
![NuevaActividad](imagenes/NuevaActividad.jpg)

### Social y Mapa
![Contactos](imagenes/Contactos.jpg)
![Búsqueda](imagenes/Búsqueda.jpg)
![DropDownMenu](imagenes/DropDownMenu.jpg)
![Mapa](imagenes/Mapa.jpg)

