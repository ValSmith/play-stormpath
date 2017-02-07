package authentication

import com.stormpath.sdk.application.Applications
import com.stormpath.sdk.client.Clients
import com.stormpath.sdk.directory.Directories
import com.stormpath.sdk.group.{Group, Groups}
import com.typesafe.config.ConfigFactory

import scala.collection.JavaConversions._
/**
  * Created by Val Smith on 10/21/16.
  * Contains global static variables for use in authenticating users.  Also ensures the groups are created for future use.
  */
object AuthenticationGlobal {
  val stormpathConfig = ConfigFactory.load.getConfig("stormpath")
  val client = Clients.builder().build()
  val application = client.getApplications(Applications.where(Applications.name().eqIgnoreCase(stormpathConfig.getString("application")))).single()
  val directory = client.getDirectories(Directories.where(Directories.name().eqIgnoreCase(stormpathConfig.getString("directory")))).single()

  val AccessTokenName = "access_token"
  val RefreshTokenName = "refresh_token"

  stormpathConfig.getStringList("groups").foreach(ensureGroupExists)

  private def ensureGroupExists(groupName: String): Unit = {
    if (groupDoesNotExist(groupName)) {
      val group = client.instantiate(classOf[Group])
        .setName(groupName)
        .setDescription("Something good")
      directory.createGroup(group)
    }
  }
  private def groupDoesNotExist(groupName: String) = {
    directory.getGroups(Groups.where(Groups.name().eqIgnoreCase(groupName))).getSize == 0
  }

}
