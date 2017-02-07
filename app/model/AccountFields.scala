package model

import com.stormpath.sdk.account.Account
import com.stormpath.sdk.group.Group
import util.MarshallableImplicits._

import scala.collection.JavaConversions._
/**
  * Created by vasmith on 12/11/16.
  */
object AccountFields {
  def toJson(stormAccount: Account) = AccountFields(stormAccount.getHref, stormAccount.getUsername, stormAccount.getEmail, stormAccount.getGivenName,
    stormAccount.getMiddleName, stormAccount.getSurname, stormAccount.getStatus.toString, stormAccount.getCreatedAt.toString,
    stormAccount.getModifiedAt.toString, stormAccount.getProviderData.getModifiedAt.toString, GroupItems(findGroups(stormAccount))).toJson

  def findGroups(stormAccount: Account): Seq[GroupFields] = stormAccount.getGroups.map(mapGroup).toSeq

  private def mapGroup(g: Group): GroupFields = GroupFields(g.getName, g.getDescription, g.getStatus.toString, g.getHref)
}


case class AccountFields(href: String, username: String, email: String, givenName:String, middleName:String, surname:String, status:String,
                         createdAt: String, modifiedAt:String, passwordModifiedAt:String, groups: GroupItems)

case class GroupItems(items: Seq[GroupFields])
case class GroupFields(name: String, description: String, status: String, href: String)