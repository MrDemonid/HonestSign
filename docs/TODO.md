```
Для корректной работы методов API параметр Accept заголовка запроса должен быть
установлен так, чтобы учитывались все возможные ответы сервера, т.е. и application/zip, и
application/json. Самый простой вариант - использовать универсальный шаблон:
Accept: */*
Для ограничения набора возвращаемых типов контента можно указывать несколько
заголовков запроса методов:
Accept: application/octet-stream
Accept: application/json

```

### Единый метод создания документов
```shell
curl 'https://ismp.crpt.ru/api/v3/lk/documents/create?pg=milk' \
-H 'content-type: application/json' -H 'Authorization: Bearer <ТОКЕН>'
--data-binary '{ "product_document":"<Документ в base64>",\
"document_format":"MANUAL", "type": "LP_INTRODUCE_GOODS",\
"signature":"<Открепленная подпись в base64>"}'
```
Тело запроса:
```json
{
 "document_format": "string",
 "product_document": "string",
 "product_group": "string",
 "signature": "string",
 "type": "string"
}
```
```text
pg - Наименование товарных групп
body:
    document_format - Тип документа: 
                            MANUAL формат json; 
                            XML формат xml; 
                            CSV формат csv;
    product_document - Содержимое документа. Base64 (JSON.stringify)
    product_group - Товарная группа:
                            Код 1 clothes – Предметы одежды, белье постельное, столовое, туалетное и кухонное;
                            Код 2 shoes – Обувные товары;
                            Код 3 tobacco – Табачная продукция;
                            Код 4 perfumery – Духи и туалетная вода;
                            Код 5 tires – Шины и покрышки пневматические резиновые новые;
                            Код 6 electronics – Фотокамеры (кроме кинокамер), фотовспышки и лампы-вспышки;
                            Код 7 pharma – Лекарственные препараты для медицинского применения;
                            Код 8 milk – Молочная продукция;
                            Код 9 bicycle – Велосипеды и велосипедные рамы;
                            Код 10 wheelchairs – Кресла-коляски
    signature - Открепленная подпись (УКЭП)
    type - Тип документа:
                            AGGREGATION_DOCUMENT – Документ агрегации. json;
                            AGGREGATION_DOCUMENT_CSV – Агрегация. csv;
                            AGGREGATION_DOCUMENT_XML – Агрегация. xml;
                            DISAGGREGATION_DOCUMENT – Дезагрегация. json;
                            DISAGGREGATION_DOCUMENT_CSV –Дезагрегация. csv;
                            DISAGGREGATION_DOCUMENT_XML – Дезагрегация. xml;
                            REAGGREGATION_DOCUMENT – Переагрегация. json;
                            REAGGREGATION_DOCUMENT_CSV – Переагрегация. csv;
                            REAGGREGATION_DOCUMENT_XML – Переагрегация. xml;
                            LP_INTRODUCE_GOODS – Ввод в оборот. Производство РФ. json;
                            LP_SHIP_GOODS – Отгрузка. json;
                            LP_SHIP_GOODS_CSV – Отгрузка. csv;
                            LP_SHIP_GOODS_XML – Отгрузка. xml;
                            LP_INTRODUCE_GOODS_CSV – Ввод в оборот. Производство РФ. csv;
                            LP_INTRODUCE_GOODS_XML – Ввод в оборот. Производство РФ. xml;
                            LP_ACCEPT_GOODS – Приемка. json;
                            LP_ACCEPT_GOODS_XML – Приемка. xml;
                            LK_REMARK – Перемаркировка;
                            LK_REMARK_CSV – Перемаркировка. csv;
                            LK_REMARK_XML – Перемаркировка. xml;
                            LK_RECEIPT – Вывод из оборота по чеку через личный кабинет. json;
                            LK_RECEIPT_XML – Вывод из оборота по чеку через личный кабинет. xml;
                            LK_RECEIPT_CSV – Вывод из оборота по чеку через личный кабинет. csv;
                            LP_GOODS_IMPORT – Ввод в оборот. Импорт. json;
                            LP_GOODS_IMPORT_CSV – Ввод в оборот. Импорт. csv;
                            LP_GOODS_IMPORT_XML – Ввод в оборот. Импорт. xml;
                            LP_CANCEL_SHIPMENT – Отмена отгрузки. Json
                            LP_CANCEL_SHIPMENT_CSV – Отмена отгрузки. csv;
                            LP_CANCEL_SHIPMENT_XML – Отмена отгрузки. xml;
                            LK_KM_CANCELLATION – Списание ненанесенных КИ. json;
                            LK_KM_CANCELLATION_CSV – Списание ненанесенных КИ. csv;
                            LK_KM_CANCELLATION_XML – Списание ненанесенных КИ. xml;
                            LK_APPLIED_KM_CANCELLATION – Списание нанесенных КИ. json;
                            LK_APPLIED_KM_CANCELLATION_CSV – Списание нанесенных КИ. csv;
                            LK_APPLIED_KM_CANCELLATION_XML – Списание нанесенных КИ. xml;
                            LK_CONTRACT_COMMISSIONING – Ввод в оборот товара. Контракт. json;
                            LK_CONTRACT_COMMISSIONING_CSV – Ввод в оборот товара. Контракт. csv;
                            LK_CONTRACT_COMMISSIONING_XML – Ввод в оборот товара. Контракт. xml;
                            LK_INDI_COMMISSIONING – Ввод в оборот товара. ФизЛицо. json;
                            LK_INDI_COMMISSIONING_CSV – Ввод в оборот товара. ФизЛицо. csv;
                            LK_INDI_COMMISSIONING_XML – Ввод в оборот товара. ФизЛицо. xml;
                            LP_SHIP_RECEIPT – Вывод отгрузкой. json;
                            LP_SHIP_RECEIPT_CSV – Вывод отгрузкой. csv;
                            LP_SHIP_RECEIPT_XML – Вывод отгрузкой.xml;
                            OST_DESCRIPTION – Описание остатков товара. json;
                            OST_DESCRIPTION_CSV – Описание остатков товара. csv;
                            OST_DESCRIPTION_XML – Описание остатков товара. xml;
                            CROSSBORDER – Трансгран. json;
                            CROSSBORDER_CSV – Трансгран. csv;
                            CROSSBORDER_XML – Трансгран. xml;
                            LP_INTRODUCE_OST – Ввод в оборот остатков. json;
                            LP_INTRODUCE_OST_CSV – Ввод в оборот остатков. csv;
                            LP_INTRODUCE_OST_XML – Ввод в оборот остатков. xml;
                            LP_RETURN – Возврат в оборот. json;
                            LP_RETURN_CSV – Возврат в оборот. csv;
                            LP_RETURN_XML – Возврат в оборот. xml;
                            LP_SHIP_GOODS_CROSSBORDER – Отгрузка при трансграничной торговли. json;
                            LP_SHIP_GOODS_CROSSBORDER_CSV – Отгрузка при трансграничной торговли. csv;
                            LP_SHIP_GOODS_CROSSBORDER_XML – Отгрузка при трансграничной торговли. xml
                            LP_CANCEL_SHIPMENT_CROSSBORDER – Отмена отгрузки при трансграничной торговли. Производство РФ. json
```
Ответ:
```json
{
"value": "9abd3d41-76bc-4542-a88e-b1f7be8130b5"
}
```
value - Уникальный идентификатор документа в ИС МП.



