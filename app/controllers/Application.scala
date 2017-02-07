package controllers

import authentication.AuthenticatedAction
import play.api.mvc._
import util.MarshallableImplicits._

class Application extends Controller {

  def index = Action { request =>
    Ok(views.html.index())
  }

  case class AwesomeThing(info: String, name: String)
  private val basicThings = Seq(
    AwesomeThing("something for everyone", "generic link"),
    AwesomeThing("lots of things", "basic"))

  private val funUserThings = Seq(
    AwesomeThing("just for the user", "specific link"),
    AwesomeThing("something else", "complicated")
  )

  def awesomeThings = AuthenticatedAction {
    Ok(funUserThings.toJson).as("application/json")
  }.notAuthenticated {
    Ok(basicThings.toJson).as("application/json")
  }

  def adminThings = AuthenticatedAction(Ok("You are seeing something for the admin"))
    .forRoles(Set("admins"))
    .notAuthenticated(Ok("Only the admins could see this"))

  def userThings = AuthenticatedAction(Ok("Lots of cool user things"))
    .forRoles(Set("users"))
    .notAuthenticated(Ok("Only the users could see this"))
}