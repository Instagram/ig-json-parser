/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.instagram.common.json.app.igmodel;

import com.instagram.common.json.annotation.JsonField;
import com.instagram.common.json.annotation.JsonType;
import java.util.List;

@JsonType
public class IgModelRequest {
  @JsonField(fieldName = "meta")
  Meta meta;

  @JsonField(fieldName = "data")
  List<Data> data;
}

@JsonType
class Meta {
  @JsonField(fieldName = "code")
  int code;
}

@JsonType
class Data {
  @JsonField(fieldName = "attribution")
  String attribution;

  @JsonField(fieldName = "tags")
  List<String> tags;

  @JsonField(fieldName = "type")
  String type;

  @JsonField(fieldName = "location")
  Location location;

  @JsonField(fieldName = "comments")
  CommentMeta comments;

  @JsonField(fieldName = "filter")
  String filter;

  @JsonField(fieldName = "created_time")
  String created_time;

  @JsonField(fieldName = "link")
  String link;

  @JsonField(fieldName = "likes")
  LikeMeta likes;

  @JsonField(fieldName = "images")
  ImageMeta images;

  @JsonField(fieldName = "users_in_photo")
  List<TaggedUser> users_in_photo;

  @JsonField(fieldName = "caption")
  Caption caption;

  @JsonField(fieldName = "user_has_liked")
  boolean user_has_liked;

  @JsonField(fieldName = "id")
  String id;

  @JsonField(fieldName = "user")
  User user;
}

@JsonType
class Location {
  @JsonField(fieldName = "latitude")
  float latitude;

  @JsonField(fieldName = "longitude")
  float longitude;

  @JsonField(fieldName = "name")
  String name;

  @JsonField(fieldName = "id")
  long id;
}

@JsonType
class CommentMeta {
  @JsonField(fieldName = "count")
  int count;

  @JsonField(fieldName = "data")
  List<Comment> data;
}

@JsonType
class LikeMeta {
  @JsonField(fieldName = "count")
  int count;

  @JsonField(fieldName = "data")
  List<User> data;
}

@JsonType
class ImageMeta {
  @JsonField(fieldName = "low_resolution")
  ImageData low_resolution;

  @JsonField(fieldName = "thumbnail")
  ImageData thumbnail;

  @JsonField(fieldName = "standard_resolution")
  ImageData standard_resolution;
}

@JsonType
class TaggedUser {
  @JsonField(fieldName = "position")
  Position position;

  @JsonField(fieldName = "user")
  User user;
}

@JsonType
class Caption {
  @JsonField(fieldName = "created_time")
  String created_time;

  @JsonField(fieldName = "text")
  String text;

  @JsonField(fieldName = "from")
  User from;

  @JsonField(fieldName = "id")
  String id;
}

@JsonType
class Comment {
  @JsonField(fieldName = "created_time")
  String created_time;

  @JsonField(fieldName = "text")
  String text;

  @JsonField(fieldName = "from")
  User from;

  @JsonField(fieldName = "id")
  String id;
}

@JsonType
class User {
  @JsonField(fieldName = "username")
  String username;

  @JsonField(fieldName = "website")
  String website;

  @JsonField(fieldName = "profile_picture")
  String profile_picture;

  @JsonField(fieldName = "full_name")
  String full_name;

  @JsonField(fieldName = "bio")
  String bio;

  @JsonField(fieldName = "id")
  String id;
}

@JsonType
class ImageData {
  @JsonField(fieldName = "url")
  String url;

  @JsonField(fieldName = "width")
  int width;

  @JsonField(fieldName = "height")
  int height;
}

@JsonType
class Position {
  @JsonField(fieldName = "x")
  float x;

  @JsonField(fieldName = "y")
  float y;
}
