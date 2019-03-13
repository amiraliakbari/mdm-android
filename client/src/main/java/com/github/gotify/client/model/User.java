/*
 * Gotify REST-API.
 * This is the documentation of the Gotify REST-API.  # Authentication In Gotify there are two token types: __clientToken__: a client is something that receives message and manages stuff like creating new tokens or delete messages. (f.ex this token should be used for an android app) __appToken__: an application is something that sends messages (f.ex. this token should be used for a shell script)  The token can be either transmitted through a header named `X-Gotify-Key` or a query parameter named `token`. There is also the possibility to authenticate through basic auth, this should only be used for creating a clientToken.  \\---  Found a bug or have some questions? [Create an issue on GitHub](https://github.com/gotify/server/issues)
 *
 * OpenAPI spec version: 1.0.6
 * 
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */


package com.github.gotify.client.model;

import java.util.Objects;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.IOException;

/**
 * The User holds information about permission and other stuff.
 */
@ApiModel(description = "The User holds information about permission and other stuff.")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2019-02-17T19:42:51.206+01:00")
public class User {
  @SerializedName("admin")
  private Boolean admin = null;

  @SerializedName("id")
  private Integer id = null;

  @SerializedName("name")
  private String name = null;

  public User admin(Boolean admin) {
    this.admin = admin;
    return this;
  }

   /**
   * If the user is an administrator.
   * @return admin
  **/
  @ApiModelProperty(example = "true", value = "If the user is an administrator.")
  public Boolean isAdmin() {
    return admin;
  }

  public void setAdmin(Boolean admin) {
    this.admin = admin;
  }

   /**
   * The user id.
   * @return id
  **/
  @ApiModelProperty(example = "25", required = true, value = "The user id.")
  public Integer getId() {
    return id;
  }

  public User name(String name) {
    this.name = name;
    return this;
  }

   /**
   * The user name. For login.
   * @return name
  **/
  @ApiModelProperty(example = "unicorn", required = true, value = "The user name. For login.")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    User user = (User) o;
    return Objects.equals(this.admin, user.admin) &&
        Objects.equals(this.id, user.id) &&
        Objects.equals(this.name, user.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(admin, id, name);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class User {\n");
    sb.append("    admin: ").append(toIndentedString(admin)).append("\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }

}

