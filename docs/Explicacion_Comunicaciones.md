# Explicación Comunicaciones

## Topics

- `road/RS-id/alerts`: Aquí se reportan los accidentes en la carretera. Las carreteras estan subscritas a este topic para informar a otros vehiculos.
- `road/RS-id/traffic`: Aquí los vehiculos reportan cuando entran y salen de la carretera. Las carreteras estan subscritas para informar del tráfico que tienen.
- `road/RS-id/info`: Aquí las carreteras reportan información de sus eventos y estados para informar a otros dispositivos.
- `road/RS-id/bins`: Aquí las basuras informan cuando entran y salen de una carretera para informar de su localización y que tipos de contenedores estan ahí en ese momento. Los camiones de la basura y las carreteras estan subscritos a este topic para tener información de las basuras y poder informar a otros vehículos.
- `bins/sensors`: Aquí las basuras informan de su estado actual. Los camiones de la basura estan subscritos para saber que basuras necesitan ser recogidas.

## Dispositivos

- Vehículos: Estos publican en `road/RS-id/alerts` para informar de accidentes y en `road/RS-id/traffic` para indicar cuando entran y salen de un segmento de carretera. También estan subscritos a `road/RS-id/info` para tener información en tiempo real del estado de las carreteras, este último lo pueden usar para saber que contenedores hay en cierta carretera.
- Carreteras: Estas estan subscritas en `road/RS-id/alerts`, `road/RS-id/traffic` y `road/RS-id/bins`, toda esta información la procesan, guardan su estado e informan de todo en `road/RS-id/info`.
- Contenedores de Basura: Estos publican en `road/RS-id/bins` para informar cuando entran y salen de una carretera. También publican en `bins/sensors` para informar de su estado actual. Estan subscritas a `road/RS-id/info` para obtener información de las carreteras, en base a eso pueden tomar decisiones para moverse a carreteras próximas y así facilitar el tráfico y la recogida de basura.
- Camiones de Basura: Estos son una extensión de los Vehículos, se comunican por todos los mismos canales pero también tienen información adicional de las basuras. Estan subscritos a `bins/sensors` para saber que basuras necesitan recogerse y de que tipo. También se subscriben a `road/RS-id/bins` para tener las posiciones actuales de todos los cubos de basura y poder trazar la ruta óptima.

## Estructura de los mensajes

...
