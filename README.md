## Week of 06/01/2025
Started development on my project, thankfully one of my previous projects, StudyPath, uses alot of the code I wanted to use for setup; so for a good few of the first commits, it wont be anything special and it will be just mostly setup/boiler plate code

google-services.json file was accidentantly included in a commit ( you live and you learn ), I used BFG Repo-Cleaner to ensure it was removed from the commits, but I have also updated and changed the google-services file to ensure security

Got authenticaion and firebase set up for my DB, its my first time using cloud Firestore so I cant wait to see all the ways it can go wrong :D, Have the basics working now anyway with a Users, Beacons and Congifurations table set up and Users are sucesfully being saved to the DB aswell as the Firebase Auth service - Also I have used some open source software for hashing the passwords -> https://github.com/jeremyh/jBCrypt

## Update for 24/01/2025
Back to college now so the development has slowed a little as alot of my focus has moved over to documentation, however I have made some nice progress from the last few weeks

Bluetooth scanning functionality has been implemented, so now the application is able to scan for BLE devices - on top of that I have "implemented" my first task which was the automated messaging, I put implemented in quotations because while the feature does work, I havent set up the connecting to the beacon functionality yet, so the task can be configured e.g selecting a contact and inputing a message, and the test button works (Test button will be on the confirmation screen for each task to give the user the option to test the configuration to ensure it works to their expectations) but not yet saved. 

Next order of buisness is going to be removing the place holder beacons on the home screen and instead getting the connect to beacon functions working so that the user can actually save the automated messaging task for instance. 
