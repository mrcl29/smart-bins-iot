# Explicación Comunicaciones

## Topics

- `<topic-base>/road/{id-segmento}/alerts`: Aquí se reportan los accidentes en la carretera. Las carreteras estan subscritas a este topic para informar a otros vehiculos.
- `<topic-base>/road/{id-segmento}/traffic`: Aquí los vehiculos reportan cuando entran y salen de la carretera. Las carreteras estan subscritas para informar del tráfico que tienen.
- `<topic-base>/road/{id-segmento}/info`: Aquí las carreteras reportan información de sus eventos y estados para informar a otros dispositivos.
- `<topic-base-aws>/road/{id-segmento}/bins`: Aquí las basuras informan cuando entran y salen de una carretera para informar de su localización y que tipos de contenedores estan ahí en ese momento. Los camiones de la basura y las carreteras estan subscritos a este topic para tener información de las basuras y poder informar a otros vehículos.
- `<topic-base-aws>/bins/sensors`: Aquí las basuras informan de su estado actual. Los camiones de la basura estan subscritos para saber que basuras necesitan ser recogidas.

El `topic-base` a usar es: `es/upv/pros/tatami/smartcities/traffic/PTPaterna`. El `topic-base-aws` a usar es: `es/upv/aws/`. Los topics de `<topic-base>/road/{id-segmento}/alerts`, `<topic-base>/road/{id-segmento}/traffic` y `<topic-base>/road/{id-segmento}/info` se encuentran en el bróker MQTT de `tcp://tambori.dsic.upv.es:10083`, mientras que los topics `<topic-base>/road/{id-segmento}/bins` y `<topic-base>/bins/sensors` irán por el bróker MQTT de AWS IoT configurado según las variables de entorno.

## Dispositivos

- Vehículos: Estos publican en `road/{id-segmento}/alerts` para informar de accidentes y en `road/{id-segmento}/traffic` para indicar cuando entran y salen de un segmento de carretera. También estan subscritos a `road/{id-segmento}/info` para tener información en tiempo real del estado de las carreteras, este último lo pueden usar para saber que contenedores hay en cierta carretera.
- Carreteras: Estas estan subscritas en `road/{id-segmento}/alerts`, `road/{id-segmento}/traffic` y `road/{id-segmento}/bins`, toda esta información la procesan, guardan su estado e informan de todo en `road/{id-segmento}/info`.
- Contenedores de Basura: Estos publican en `road/{id-segmento}/bins` para informar cuando entran y salen de una carretera. También publican en `bins/sensors` para informar de su estado actual. Estan subscritas a `road/{id-segmento}/info` para obtener información de las carreteras, en base a eso pueden tomar decisiones para moverse a carreteras próximas y así facilitar el tráfico y la recogida de basura.
- Camiones de Basura: Estos son una extensión de los Vehículos, se comunican por todos los mismos canales pero también tienen información adicional de las basuras. Estan subscritos a `bins/sensors` para saber que basuras necesitan recogerse y de que tipo. También se subscriben a `road/{id-segmento}/bins` para tener las posiciones actuales de todos los cubos de basura y poder trazar la ruta óptima.

## Ejemplo de estructura de los mensajes

### `<topic-base>/road/{id-segmento}/alerts`

```json
{
  "id":"MSG_1477472671831",
  "type":"ROAD_INCIDENT",
  "timestamp":1477472671831,
  "msg":{
    "rt":"traffic::alert",
    "incident-type":"TRAFFIC_ACCIDENT",
    "id":"7129632011",
    "road":"R2",
    "road-segment":"R2S1",
    "starting-position":10,
    "ending-position":10,
    "description":"Vehicle Crash",
    "status":"Active",
    "link":"/incident/7129632011"
  }
}
```

### `<topic-base>/road/{id-segmento}/traffic`

Para mensaje `VEHICLE_IN` al entrar en un Road Segment:

```json
{
  "id":"MSG_1477473530870",
  "type":"TRAFFIC",
  "timestamp":1477473530870,
  "msg":{
    "action":"VEHICLE_IN",
    "road":"R1",
    "road-segment":"R1S4a",
    "vehicle-id":"SC1477473368403736",
    "position":520,
    "role":"MedicalAssistance"
  }
}
```

Para mensaje `CHECK_IN` (actualización de posición) en un Road Segment:

```json
{
  "id":"MSG_147747746948753",
  "type":"TRAFFIC",
  "timestamp":1477746948753,
  "msg":{
    "action":"CHECK_IN",
    "road":"R1",
    "road-segment":"R1S4a",
    "vehicle-id":"SC1477473368403736",
    "position":580,
    "role":"MedicalAssistance"
  }
}
```

Para mensaje `VEHICLE_OUT` al salir de un Road Segment:

```json
{
  "id":"MSG_1477489498731",
  "type":"TRAFFIC",
  "timestamp":1477489498731,
  "msg":{
    "action":"VEHICLE_OUT",
    "road":"R1",
    "road-segment":"R1S4a",
    "vehicle-id":"SC1477473368403736",
    "position":640,
    "role":"MedicalAssistance"
  }
}
```

### `<topic-base>/road/{id-segmento}/info`

Para mensaje de estado carretera (RoadSegment Status):

```json
{
  "id":"MSG_1477474100254",
  "type":"ROAD_STATUS",
  "timestamp":1477474100254,
  "msg":{
    "rt":"road-segment",
    "road":"R11",
    "road-segment":"R11S3",
    "start-kp":0,
    "end-kp":377,
    "length":377,
    "capacity":8,
    "num-vehicles":2,
    "density":25,
    "status":"Mostly_Free_Flow",
    "max-speed":30,
    "current-max-speed":30,
    "code":"1",
    "link":"/segment/R11S3"
  }
}
```

Para mensaje de incidencia en carretera (RoadIncident):

```json
{
  "id":"MSG_1477472669651",
  "type":"ROAD_INCIDENT",
  "timestamp":1477472669651,
  "msg":{
    "rt":"traffic::incident",
    "incident-type":"INCIDENT",
    "id":"712963150",
    "road":"R8",
    "road-segment":"R8S1",
    "starting-position":584,
    "ending-position":684,
    "description":"Working Area",
    "status":"Active",
    "link":"/incident/712963150"
  }
}
```

### `<topic-base>/road/{id-segmento}/bins`

```json
{
  "id":"MSG_1477472669651",
  "type":"BIN_POSITION",
  "timestamp":1477472669651,
  "msg":{
    "deviceId":"BIN_4321412",
    "road":"R11",
    "road-segment":"R11S3",
    "kp":500,
    "action": "BIN_OUT", // BIN_OUT or BIN_IN
    "type":"PLASTIC",
  }
}
```

### `<topic-base>/bins/sensors`

```json
{
  "id":"MSG_1477472669651",
  "type":"BIN_SENSOR",
  "timestamp":1477472669651,
  "msg":{
    "deviceId":"BIN_34242",
    "road":"R11",
    "road-segment":"R11S3",
    "kp":500,
    "level":95, // 95%
    "toClean":true,
    "type":"PLASTIC",
  }
}
```
