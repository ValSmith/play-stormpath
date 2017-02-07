# play-stormpath
This project provides a template for any web application that requires user Authentication/Authorization.  The backend is 
[Scala Play framework].  The frontend is built on [AngularJS] with [Stormpath AngularJS].  The angular ui router is used
instead of ng route as it is required by the stormpath module. The Scala template is only used to load the index page as I have
found angular to be a bit easier to use and has a lot more community support.  The user database is hosted by [Stormpath],
an authentication and authorization service and implemented using [their java api].  The front end design is from a stormpath
example project, the only modifications are to add some group functionality

The only steps you need to get it going are:
* Have [scala - sbt] installed
* Create a [Stormpath] account

## 1. Install Scala Sbt

Download and install here: [scala - sbt]

## 2. Create a [Stormpath] account

Follow instructions at [Stormpath]
Use your api key and create a property file ~/.stormpath/apiKey.properties
```
apiKey.id = ###
apiKey.secret = ###
```
Create an application and directory using the [Stormpath] web client.  Set those values in the [application.conf] file

## 2.5 (Optional) Set up email verification

By default after creating an account you are redirected to the login page and can immediately log in.  If you want to verify the 
provided email address browse to the directory in the [Stormpath] -> Workflows & Emails -> Verification Email.  Set the Link 
Base URL to http://localhost:9000/verifyPage.

## 3. Run application

Use sbt to run the application
```
sbt run
```
Browse to http://localhost:9000

## 4. Add your own endpoints

Now you are set up to start developing.  To add a secure endpoint use the class AuthenticatedAction.  It mimics the Action class
from the play framework.

[application.conf]:conf/application.conf
[scala - sbt]:http://www.scala-sbt.org/ 
[their java api]: https://docs.stormpath.com/java/product-guide/latest/index.html
[Stormpath]: https://stormpath.com
[Stormpath AngularJS]: https://docs.stormpath.com/angularjs/sdk/#/api 
[AngularJS]: https://angularjs.org/
[Scala Play framework]: https://www.playframework.com/