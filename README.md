# MyChallenge-AppDirect
This is spring boot project for AppDirect coding challenge. I created an app call CL (not published)
The url:https://chang.cfapps.io
This app that created is call `CL`, which is not published

It supports：
- Subscription order event
- Subscription change event
- Subscription cancel event
- Subscription notice event
- User assignment event
- User unassignment event
- Single sign-on via OPENID

Only minimal html is shown in the homepage 

It uses a ORM called GenericDAO by Professor Jeff Eppinger. So, to run it:
```
	- install this first by run `genericdao/install.sh` 
		(This uses wget command.The other option is to download the
		`http://www.jeffeppinger.com/GenericDAO/genericdao-2.0.2.jar`
		and add it to external jar)
	
	- then run `mvn spring-boot:run`
