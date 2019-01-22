##############################################################################################
############################### Property of Motorola Solutions ###############################
##############################################################################################
MotoDynDNS is a Custom Dynamic DNS solution, used for updating a Dynamic DNS server with an Android device's IP address and hostname.
The whole solution is based on a Bind9 Dynamic DNS, running on a RedHat7 platform, but should work on any platform running Bind9, "nsupdate" and Python (both v2 and v3).

The project consists of four parts:
Android Application - Configuration
Android Service - Communication with remote server
Python Script - communication with client
Bash Script - Dynamic DNS update

Android Application v1.0:
The application allows the configuration of a remote server and specific port, a hostname and domain and the required entry type and TTL of the updated entry.
The application runs a never-ending (unless force stopped) service that runs in the background.

Android service v1.0:
The service works in conjunction with a Python script and a Bash script based on "nsupdate". Both scripts are available at this repository, under "scripts".
The service sends the configured Hostname to a remote server running the Python script.
The message is sent periodically (hardcoded - every 10 minutes) and on any connectivity change (such as new dchp IP address).

Python script:
Opens a listening socket on port 1024 (may be manually changed to any other open and available port).
Receives HTTP messages containing a JSON object and grabs the JSON_OBJ["hostname"] value.
The IP address of the client is taken from the socket.
The script then uses a Bash script to update the hostname + IP address on the Bind9 server.

Bash script:
Runs "nsupdate" tool with the required key.
The script takes two values (hostname and IP address) and updates the entry on the Bind9 server.

##############################################################################################
########################################## Disclaimer ########################################
##############################################################################################
This solution was created for a specific LAB need and may not be adapted for commercial use. The use of this application is at the responsibility of the user.
Motorola Solutions is not be held accountable for the misuse of this application or any harm that may occur to a user's devices.
For requested features, please contact the repository owner.



