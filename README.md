# Easy Cooking

## Table of Contents
1. [Overview](#Overview)
1. [Product Spec](#Product-Spec)
1. [Wireframes](#Wireframes)
2. [Schema](#Schema)

## Overview
### Description
Enables user to manage shopping list, pantry records and recipes in one app. The app utilizes [Edamam Recipe Search API](https://developer.edamam.com/edamam-docs-recipe-api) and [YouTube API](https://developers.google.com/youtube/v3).

### App Evaluation
- **Category:** Productivity
- **Mobile:** This app would be primarily developed for a mobile device because of its portability.
- **Story:** Cooking is difficult and requires a lot of executive planning which may be difficult for some people. This app aims to make the process a little easier by collating all you need in one place.
- **Market:** Anyone can use this app, particularly adults who manage their own chores
- **Habit:** This app should be used often as it manages user needs for daily living (cooking, getting groceries)
- **Scope:** Pretty limited in scope, could possibly expand to more social app for sharing recipes (and not just making personal recipes or getting recipes from an API)

## Product Spec

### 1. User Stories (Required and Optional)

**Required Must-have Stories**

* [x] User must sign up/in to use app
* [x]User can view and edit (add and delete) grocery shopping list on app
   * [x] Each item has a "delete" button and a "bought" button to move item to pantry list
   * [x] User can move items from grocery shopping list to pantry directly
* [x] User can view and edit (add and delete) of ingredients in their pantry list
    * [x] Each item has a delete button to remove list from pantry
* [x] User can search recipes with given ingredients and view them e.g. [Edamam](https://developer.edamam.com/edamam-docs-recipe-api)
* [x] User can search video recipes with prep instructions and play the video in app e.g. [Youtube](https://developers.google.com/youtube/v3/getting-started)


**Optional Nice-to-have Stories**
* [x] Each item in grocery/pantry list has an image shown
* [x] User can save and unsave recipes
* [x] User can view saved recipes
* [x] User can create own recipes which are automatically saved on their device
* [x] User can specify dietary restrictions that are applied by default to all recipe search (like dairy-free, nut-free, vegan, vegetarian, gluten-free)
  * [x] User can edit filters with each search or from account settings
* [ ] User can input expiration dates/freshness limits on groceries and receive push notifications closer to said date to prevent wastage
    * [x] Items in pantry must be clickable/editable
* [x] User can select things in pantry list and implement recipe search directly
* [x] User can have recipes suggested based on what they have in pantry list
  * [x] User can add ingredients they do not have to grocery list
* [ ] User can see a how to use this app page
* [ ] User can take pictures of groceries, recipes that they make, etc. and add them
* [ ] User can search and save grocery locations (using GoogleMaps api)
* [x]  User can recover forgotten password through emails
* [ ]  User can search synonyms on Recipe Search (lemmitization). For example searching for banana vs bananas returns similar result.
* [x]  User can load more results on Recipe Search after first 20 are shown.


### 2. Screen Archetypes

* Register
* Login
   * Upon download of the application, user is prompted to log in.
* Stream
   * User can view grocery shopping list and delete item
* Create
   * User can add new item to grocery shopping list
* Stream
   * User can view pantry list and delete item
* Create
   * User can add new item to pantry list
  * (stretch) User can edit information on pantry item after it is created e.g. expiration dates
* Stream
  * User can search recipes gotten from online recipe database
* Stream (optional)
    * User can view their saved recipes and username
* Detail
    * User can view recipes in detail
* Create (optional)
    * User can create own recipes


### 3. Navigation

**Tab Navigation** (Tab to Screen)

* Grocery List
* Pantry list
* User Profile
* Recipe search
* Video recipes
* Text recipes


**Flow Navigation** (Screen to Screen)

* Login screen
   * => Register screen, if user does not already have account
   * => Grocery list
 * Register screen
   * => Grocery list
* Grocery List
   * => Create grocery item
   * => (stretch) search for recipe with selected grocery item
* Grocery item
  * => Grocery List 
* Pantry List
  * => Create pantry item
* Pantry item
  * => Pantry List
* Saved recipes
  * => Create new recipe
* New Recipe
  * => Saved recipes


## Wireframes
[Add picture of your hand sketched wireframes in this section]
<img src="IMG-3501.jpg" width=600>

### [BONUS] Digital Wireframes & Mockups

### [BONUS] Interactive Prototype

## Schema 
### Models
**Food items**
|Property |Type           |Description                                           |
|---------|---------------|------------------------------------------------------|
|objectId |String         |unique id for the food item (default field)           |
|name     |String         |name of item.                                         |
|quantity |number         |amount of food item                                   |
|measure  |String         |quantity in which food item is measured               |
|user     |pointer to User|food item author                                      |
|updatedAt|DateTime       |date when post is created (default field)             |
|createdAt|DateTime       |date when post is last updated (default field)        |
|type     |String         |type of food item (grocery, pantry, recipe ingredient)|

**User**
|Property     |Type           |Description                                   |
|-------------|---------------|----------------------------------------------|
|objectId     |String         |unique id for the user (default field)        |
|username     |String         |name of user                                  |
|password     |String         |user's password                               |
|updatedAt    |DateTime       |date when post is created (default field)     |
|createdAt    |DateTime       |date when post is last updated (default field)|
|email        |String         |email address of user                         |
|emailVerified|boolean        |whether email address is verified or not      |


**Recipe**
|Property   |Type           |Description                                   |
|-----------|---------------|----------------------------------------------|
|objectId   |String         |unique id for the recipe (default field)      |
|name       |String         |recipe name                                   |
|updatedAt  |DateTime       |date when post is created (default field)     |
|createdAt  |DateTime       |date when post is last updated (default field)|
|user       |pointer to User|recipe author                                 |
|ingredients|Array          |Array of food item objects                    |
|procedure  |String         |String array of steps for preparing food      |

**Video**
|Property    |Type           |Description                                   |
|------------|---------------|----------------------------------------------|
|objectId    |String         |unique id for the recipe (default field)      |
|title       |String         |video title                                   |
|updatedAt   |DateTime       |date when post is created (default field)     |
|createdAt   |DateTime       |date when post is last updated (default field)|
|user        |pointer to User|user who saved video                          |
|thumbnailURL|String         |image url of video thumbnail                  |


### Networking
* grocery/pantry list screen: 
    * get request for all food items in list where user is author
```
ParseQuery<FoodItem> query = ParseQuery.getQuery(FoodItem.class);
        // include data where post is current post
        query.whereEqualTo("type", type); // type is either "grocery" or "pantry"
        query.whereEqualTo("user", ParseUser.getCurrentUser());
        // necessary to include non-primitive types
        query.include("user");
        // order posts by creation date (newest first)
        query.addDescendingOrder("createdAt");
        query.findInBackground();   
```

* saved/user-created recipes: 
```
ParseQuery<Recipe> query = ParseQuery.getQuery(Recipe.class);
        // include data where recipe is given type and was saved/created by current user
        query.whereEqualTo("type", type); // type is either "user" or "saved"
        query.whereEqualTo("user", ParseUser.getCurrentUser());
        // necessary to include non-primitive types
        query.include("user");
        // order posts by creation date (newest first)
        query.addDescendingOrder("createdAt");
        query.findInBackground(new FindCallback<Recipe>()
```

## Credits/Attribution
* Icons generated by [frepik](https://www.flaticon.com/free-icons/list)
   * Shelf icons created by [Freepik](https://www.flaticon.com/free-icons/shelf)
   * List icons created by [Freepik](https://www.flaticon.com/free-icons/list)
   * Pan icons created by [Good Ware](https://www.flaticon.com/free-icons/pan)
   * Nutrition background vector created by p[ikisuperstar](https://www.freepik.com/vectors/nutrition-background) - www.freepik.com
   * Canned food vector created by [brgfx](https://www.freepik.com/vectors/canned-food) - www.freepik.com
   * Etching vector created by [rawpixel.com](https://www.freepik.com/vectors/etching) - www.freepik.com
   * Kale vector created by [pch.vector](https://www.freepik.com/vectors/kale) - www.freepik.com
   * Cog vector created by [rawpixel.com](https://www.freepik.com/vectors/cog) - www.freepik.com
   * Milk icons created by [Freepik](https://www.flaticon.com/free-icons/milk) - Flaticon
   * Spinach icons created by [Eucalyp](https://www.flaticon.com/free-icons/spinach) - Flaticon
 * Bone icons created by [smalllikeart](https://www.flaticon.com/free-icons/bone) - Flaticon
 * Rice icons created by [Icongeek26](https://www.flaticon.com/free-icons/rice) - Flaticon
 * Soy sauce icons created by [mangsaabguru](https://www.flaticon.com/free-icons/soy-sauce) - Flaticon
 * Expand icons created by [Google](https://www.flaticon.com/free-icons/expand) - Flaticon
 * Salt icons created by [Freepik](https://www.flaticon.com/free-icons/salt) - Flaticon
 * Fruit icons created by [Smashicons](https://www.flaticon.com/free-icons/fruit) - Flaticon

## License

    Copyright 2022 Mezisashe Ojuba

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
