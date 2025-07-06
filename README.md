# TempbanSM

Un mod simple de NeoForge que agrega el comando `/tempban` que funciona de manera similar al comando `/ban` de Minecraft, con la diferencia de que puedes especificar la duración del baneo en horas, días y meses.

Este mod utiliza el campo 'expired' de Minecraft que no se usa en el comando de baneo normal.

## Uso

```
/tempban <jugador> <meses> <días> <horas> [<razón>]
```

**Ejemplos:**
- `/tempban Steve 0 1 0` - Banea a Steve por 1 día
- `/tempban Alex 0 0 2 Descanso obligatorio` - Banea a Alex por 2 horas con razón
- `/tempban Herobrine 1 0 0 Baneo mensual` - Banea a Herobrine por 1 mes

Para desbanear usuarios que fueron baneados temporalmente, usar el comando vanilla `/pardon` funciona correctamente.

## Comandos Disponibles

### `/tempban`
Banea un jugador temporalmente por el tiempo especificado.

**Sintaxis:** `/tempban <jugador> <meses> <días> <horas> [<razón>]`
- `<jugador>`: Nombre del jugador a banear
- `<meses>`: Número de meses (0 o más)
- `<días>`: Número de días (0 o más)
- `<horas>`: Número de horas (0 o más)
- `[<razón>]`: Razón del baneo (opcional)

**Nota:** Al menos uno de los valores de tiempo debe ser mayor que 0.

### `/banlist`
Muestra la lista de jugadores baneados con información detallada incluyendo fechas de expiración.

**Variantes:**
- `/banlist` - Muestra todos los baneos (jugadores e IPs)
- `/banlist players` - Muestra solo jugadores baneados
- `/banlist ips` - Muestra solo IPs baneadas

## Características

- **Baneo temporal preciso**: Especifica duración exacta en meses, días y horas
- **Lista de baneos mejorada**: Muestra fechas de creación y expiración
- **Mensajes personalizados**: Interfaz completamente en español
- **Integración vanilla**: Compatible con comandos de Minecraft existentes
- **Configuración personalizable**: Mensajes modificables desde el archivo de configuración
- **Validación robusta**: Previene baneos de duración cero

## Notas Técnicas

Dado que el campo 'expired' no se usa extensivamente en el código de vanilla, los usuarios que fueron baneados temporalmente pueden enfrentar problemas menores al unirse a un servidor (por ejemplo, no poder conectarse ocasionalmente debido a excepciones). Si ocurren problemas más graves, no dudes en reportar un issue aquí en GitHub.

## Instalación

**Este mod es solo para servidor** - los jugadores no necesitan instalarlo.

1. Descarga el archivo `.jar` del mod
2. Colócalo en la carpeta `mods` de tu servidor
3. Reinicia el servidor
4. Los comandos estarán disponibles para administradores (nivel de permisos 3)

## Requisitos

- Minecraft 1.21.1
- NeoForge 21.1.0 o superior
- Java 21

## Licencia

MIT License