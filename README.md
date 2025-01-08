## Week of 06/01/2025
Started development on my project, thankfully one of my previous projects, StudyPath, uses alot of the code I wanted to use for setup; so for a good few of the first commits, it wont be anything special and it will be just mostly setup/boiler plate code

google-services.json file was accidentantly included in a commit ( you live and you learn ), I used BFG Repo-Cleaner to ensure it was removed from the commits, but I have also updated and changed the google-services file to ensure security

Got authenticaion and firebase set up for my DB, its my first time using cloud Firestore so I cant wait to see all the ways it can go wrong :D, Have the basics working now anyway with a Users, Beacons and Congifurations table set up and Users are sucesfully being saved to the DB aswell as the Firebase Auth service - Also I have used some open source software for hashing the passwords -> https://github.com/jeremyh/jBCrypt
