# UNISC_Rides

When we were on our way to become Computer Engineers, two friends and I were tasked with creating an [Android](https://en.wikipedia.org/wiki/Android_(operating_system)) app to manage car sharing between students that were headed towards the university and students that wanted a ride.

This app would have to share a database. The most logical solution would be to host it in a web service. However, being engineers, we had no experience with web services and no time to learn about them. We discussed distributing the database on all devices and using [TCP/IP](https://en.wikipedia.org/wiki/Transmission_Control_Protocol) to keep it up to date, but that was also complex and, though we knew how to do it, it would also take a lot of time.

That's when I came up with this idea: implement a fake web service to run on a laptop, and have the phones connect through it via TCP/IP. And it worked pretty well! I implemented a [MySQL](https://en.wikipedia.org/wiki/MySQL) database and a [Java](https://en.wikipedia.org/wiki/Java_(programming_language)) `WebServiceEmulator` to bridge the phones with the "server". I also implemented a standard `WebServiceInterface` that was adapted in Android, which was the one that actually talked to the server and had methods like `insertTableX` and `readTableY`.

But that's not all! We also required a method from the [Google Maps API](https://en.wikipedia.org/wiki/Google_maps) that only existed in the [JavaScript](https://en.wikipedia.org/wiki/JavaScript) version ([RouteBoxer](http://dev.huement.com/gmaps/routeboxer/docs/examples.html), to find students wanting a ride close to a path), so I created an [HTML](https://en.wikipedia.org/wiki/HTML) page that executed this method and talked via [WebSocket](https://en.wikipedia.org/wiki/WebSocket) to the server.

I've decided to host this project here because I think that several of the solutions that I found could be useful references for other programmers. I'll post as much instructions as I can, but I can't guarantee it will run.

This project was made with [NetBeans](https://en.wikipedia.org/wiki/Netbeans) on the server side and [Android Studio](https://en.wikipedia.org/wiki/Android_Studio) on the Android side. It contains the following libraries, that are copyrights of their respective owners: [Jetty](https://en.wikipedia.org/wiki/Jetty_(web_server)) and [MySQL Connector](https://en.wikipedia.org/wiki/MySQL_Connector/ODBC) on the server side, [Google Maps API](https://en.wikipedia.org/wiki/Google_Maps#Google_Maps_API) on the client side. The necessary JARs (and maybe some unnecessary ones) have been included.

I wanted to upload the Android Studio project folder, but it contained more than 1&thinsp;600 files that could not be deleted without messing with the build process, so I just uploaded the source files. If you want to build this, you'll have to create a new project and add the sources to it. If some problems arise then, let me know and I'll try to help. Also, neither of us own the domain we used for the package names. If someone owns it, let me know and I'll change it.

## Instructions

1. Make a MySQL database.
2. Run the `banco 2015-11-22.sql` script.
3. Run the server in Linux with `sudo java -jar path/to/WebServiceEmulator.jar 22113 mysql_user_name mysql_user_password`
  1. If there's no password, use `""`.
4. Wait for `listening` on the server's console.
5. Open `WebServiceJavaScriptEngine.html` in a web browser.
  1. If you restart the server, refresh the page in the browser.
6. Wait for `RouteBoxerHandler: Connect: /127.0.0.1`.
7. Edit `WebserviceInterface.java` in the Android app and replace `serverIP` by the server IP.
8. Run the Android app.
