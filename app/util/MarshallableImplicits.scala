package util

import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper

/**
  * Created by vasmith on 10/25/16.
  */
object MarshallableImplicits {
  private val mapper = new ObjectMapper() with ScalaObjectMapper
  mapper.registerModule(DefaultScalaModule)
  mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

  implicit class Unmarshallable(unMarshallMe: String) {
    def fromJson[T]()(implicit m: Manifest[T]): T =  mapper.readValue[T](unMarshallMe)
  }

  implicit class Marshallable[T](marshallMe: T) {
    def toJson: String = mapper.writeValueAsString(marshallMe)
  }
}
