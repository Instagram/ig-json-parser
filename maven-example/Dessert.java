@JsonType
class Dessert {
  @JsonField(fieldName="type")
  String type;

  @JsonField(fieldName="rating")
  float rating;
}
