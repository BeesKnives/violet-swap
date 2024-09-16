package com.launchcode.violetSwap.models;

import com.launchcode.violetSwap.models.data.ListingRepository;
import com.launchcode.violetSwap.models.data.UserRepository;
import com.launchcode.violetSwap.models.data.VarietyRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class SearchService {

    @Autowired
    private ListingRepository listingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VarietyRepository varietyRepository;

    @Autowired
    private UserService userService;

    //fields for filtered listings and users:
    List<Listing> filteredListings = new ArrayList<Listing>();
    List<User> filteredUsers = new ArrayList<User>();
    List<Variety> filteredVarieties = new ArrayList<Variety>();

    //_____________________________________________________________________________________________________

    //todo: searchquery, gets what to search for - listing/user/variety, and searchterm.
    // Calls makeSearchTerm, then uses that to construct a query using concatination.
    // Then returns the appropriate list (?) how to have it able to return different data types??
    // Will it have to be 3 separate methods + the query constructor method?

    public String makeQueryFromSearch(String tableName, String columnName, String search){

        String table = tableName; //pick the table you are creating the query for

        if (table==null){ //if no table found, return error msg //todo: set up for receiving error msgs
            return "no repository found";
        }


        List<String> searchTerms = makeSearchTerm(search); //make search term

        String query = "SELECT * FROM ".concat(table).concat(" WHERE CONTAINS  "); //start query, including the table you will be searching in


        int countdown = searchTerms.size(); //countdown the length of the searchTerms,
        for(String searchTerm : searchTerms){
            query.concat("(").concat(columnName).concat(", ").concat(searchTerm).concat(")");//for each searchTerm, concat onto query w/ column name and searchTerm

            countdown --; //tick down on the countdown
            if(countdown != 0){
                query.concat(" AND "); //if it's not the end of the list, add an " AND " to the query
            }

        }
        return query;
    }


    //_____________________________________________________________________________________________________
public List<Listing> setFilteredListingsByVariety(Integer varietyId){ //todo: set filteredListings according to the variety id provided

    //todo:write a query to get all listings with the specific id in varietyId?

    if(varietyId==null){
        filteredListings = listingRepository.findAll();
        return filteredListings;
    }

    Variety selectedVariety = varietyRepository.findById(varietyId).orElse(null); //get variety from varietyRepository
    if(selectedVariety != null){
        filteredListings.clear();
        filteredListings.addAll(selectedVariety.getListings()); //have filtered listings be just the ones of this variety
    }else{ //if selectedVariety is null, show all listings
        filteredListings = listingRepository.findAll();
    }

    return filteredListings;
}




//_____________________________________________________________________________________________________SEARCHES
//_____________________________________________________________________________________________________

    //remove extra parts from the search term and capitalize it to make it easier to search with
    public String removeExtraChars(String string){
        String fixedString = string.toUpperCase(); // case insensitive (Uppercase)
        List<String> removeThese = Arrays.asList( "'", "-", "_", Character.toString('"')); //array [ ', -, _, "]

        for(String item : removeThese){
            fixedString = fixedString.replaceAll(item,""); // for every item present in removeThese, delete it
        }
        return fixedString;
    }
    //set up search term into a list (calls removeExtraChars)
    public List<String> makeSearchTerm (String searchTerm){
        String editedString = removeExtraChars(searchTerm); //call removeExtraChars
        List<String> editedSearchTerm = Arrays.asList(editedString.split(" "));  //convert the search term into a list
        return editedSearchTerm;
    }



    //search for varieties
    public List<Variety> searchVarieties(String search){
        filteredVarieties.clear();//so no duplicates
        List<String> searchItems = makeSearchTerm(search); //make search parameter into a list of Strings (searchItems)

        for(Variety variety : varietyRepository.findAll()){ //For each variety
            int counter = searchItems.size();
            String varietyName = removeExtraChars(variety.getName()); //get searchTerm of the variety.

            for(String searchItem : searchItems){ //For each searchItem
                if (varietyName.contains(searchItem)){ //check if varietyName contains the searchItem.
                    counter --; //if yes, mark it and move to next searchItem
                    if (counter == 0){//once counter reaches 0, all search items have been found in varietyName, and that variety can be added to the list.
                        filteredVarieties.add(variety);
                    }
                } else{
                    break;
                }
            }
        }
        return filteredVarieties;
    }

    //search for users
    public List<User> searchUsers(String search){
        filteredUsers.clear();//so no duplicates
        search = search.toUpperCase();
        for(User user : userRepository.findAll()){ //for every user
            if(user.getUsername().toUpperCase().contains(search)){ //if they contain the search term, save in filteredUsers
                filteredUsers.add(user);
            }
        }
        return filteredUsers;
    }


    //search Users by zipcode
    public List<User> filterUsersByZipcode(String searchZip){
        filteredUsers.clear();//so no duplicates
        Integer searchZipcode = Integer.valueOf(searchZip);
        for(User user : userRepository.findAll()){
            if(user.getZipcode().equals(searchZipcode)){ //if the search term equals the zipcode, add it to filteredLocationUsers
                filteredUsers.add(user);
            }
        }
        return filteredUsers;
    }

//    //search Users by city / state, needs to have
//    public List<User> filterUsersByLocation(String searchCity, String searchState){
//        filteredUsers.clear();//so no duplicates
//        searchCity = searchCity.toUpperCase();
//        searchState = searchState.toUpperCase();
//        for(User user : availableUsers){
//            if(user.getCity().toUpperCase().contains(searchCity) && user.getState().toUpperCase().contains(user.getState())){ //if the search term contains the city and state, add it to filteredLocationUsers
//                filteredUsers.add(user);
//            }
//        }
//        return filteredUsers;
//    }


    //may be moved to controller??:
    //filters users by calling methods, then gets the listings from those users
    public List<Listing> filterListingsByZipcode(String searchZip){
        filteredListings.clear();//so no duplicates
        List<User> filteredLocationUsers = filterUsersByZipcode(searchZip); //call filterUsersByLocation/Zipcode
        for(User user : filteredLocationUsers){ //for every user returned,
            List<Listing> filteredLocationListings = user.getListings(); //get the user's listings
            filteredListings.addAll(filteredLocationListings); //save the listings to filteredListings
        }
        return filteredListings;
    }





//_____________________________________________________________________________________________________ END SEARCHES
//_____________________________________________________________________________________________________

//_____________________________________________________________________________________________________ SORTING
//_____________________________________________________________________________________________________

    //sort listings by distance ascending
    public List<Listing> sortListingsByDistance(HttpServletRequest request){
        if (filteredListings.isEmpty()){
            filteredListings = listingRepository.findAll();
        }

        //code here to filter listings by distance:
        //get current user's latitude and longitude
        
        User user = userService.getUserFromSession(request.getSession()); //get user from session



        Double userLatitude = user.getLatitude(); //get lat/long of user
        System.out.println(userLatitude + "_______________________________________________________________");
        Double userLongitude = user.getLongitude();


        //get list of users in listings??


        for ( Listing listing : filteredListings ) { //for each listing in filteredListings

            if(listing.getUser().getDistance()==(null)){ //check if the listing's user's distance has already been calculated.



                //calculate distance between user and the other users:
                Double listingLatitude = listing.getUser().getLatitude(); //get lat/long of listing
                Double listingLongitude = listing.getUser().getLongitude();

                Double differenceLatitude = Math.abs(userLatitude - listingLatitude); //get distance between lat/long
                Double differenceLongitude = Math.abs(userLongitude - listingLongitude);

                // Math.sqrt(x) 	Returns the square root of x 	            double
                // Math.pow(x, y) 	Returns the value of x to the power of y 	double
                // Math.cos(x) 	    Returns the cosine of x (x is in radians) 	double


                //find the distance of the longitude using the latitude and cosine
                // miles = cosine (degrees of latitude) · 69.17 * differenceLongitude
                Double distanceLongitude = (Math.cos( differenceLatitude ) * 69.17) * differenceLongitude;

                //find distance of latitude (miles)
                Double distanceLatitude = differenceLatitude * 69;

                // find the hypotenuse of the two distances (miles) -> hypotenuse = rootOf( aSquared + bSquared)
                Double distance = Math.sqrt( Math.pow(distanceLatitude,2) + Math.pow(distanceLongitude,2)); //calculate overall distance


                distance= Math.round(distance * 10) / 10d;


                listing.getUser().setDistance(distance); //set distance in User
            }
        }
        //once distance between user and other users has been calculated,

        //order the listings by listing.getUser().getDistance() - done in Listing's compareTo override, implemented here
        Collections.sort(filteredListings);


        //TODO: this is quite clunky, we should look into using a SQL query to order the list of listings, then delete those that don't match filteredlistings?
        // Then again, the distance isn't being stored in the SQL, but the session. Not sure how to go about this in this way.
        // Maybe ask other coders what would be good for efficiency for this?

        //todo: Send the list of listing ids in the SQL query, have it pick them out and order them, and then send the data back here??





        return filteredListings;
    }

    //sort listings by distance descending
    public List<Listing> ReverseSortListingsByDistance(HttpServletRequest request) {

        sortListingsByDistance(request);
        Collections.reverse(filteredListings);

        return filteredListings;
    }







    //sort users by distance ascending
    public List<User> sortUsersByDistance(HttpServletRequest request){
        if (filteredUsers.isEmpty()){
            filteredUsers = (List<User>) userRepository.findAll();
        }

        User currentUser = userService.getUserFromSession(request.getSession()); //get user from session


        Double currentUserLatitude = currentUser.getLatitude(); //get lat/long of currentUser
        Double currentUserLongitude = currentUser.getLongitude();


        for ( User user : filteredUsers ) { //for each user in filteredUsers

            if(user.getDistance()==(null)){ //check if the user's distance has already been calculated.



                //calculate distance between user and the other users:
                Double listingLatitude = user.getLatitude(); //get lat/long of this user
                Double listingLongitude = user.getLongitude();

                //todo:have distance calculation be a separate method, send in both lats and longs, call here and in sortListingsByDistance

                Double differenceLatitude = Math.abs(currentUserLatitude - listingLatitude); //get distance between lat/long
                Double differenceLongitude = Math.abs(currentUserLongitude - listingLongitude);


                //find the distance of the longitude using the latitude and cosine
                // miles = cosine (degrees of latitude) · 69.17 * differenceLongitude
                Double distanceLongitude = (Math.cos( differenceLatitude ) * 69.17) * differenceLongitude;

                //find distance of latitude (miles)
                Double distanceLatitude = differenceLatitude * 69;

                // find the hypotenuse of the two distances (miles) -> hypotenuse = rootOf( aSquared + bSquared)
                Double distance = Math.sqrt( Math.pow(distanceLatitude,2) + Math.pow(distanceLongitude,2)); //calculate overall distance


                distance= Math.round(distance * 10) / 10d;


                user.setDistance(distance); //set distance in user
            }
        }
        //once distance between currentUser and other users has been calculated,
        //order the listings by user.getDistance() - done in User's compareTo override, implemented here
        Collections.sort(filteredUsers);

        return filteredUsers;
    }

    //sort users by distance descending
    public List<User> ReverseSortUsersByDistance(HttpServletRequest request) {

        sortUsersByDistance(request);
        Collections.reverse(filteredUsers);

        return filteredUsers;
    }

//_____________________________________________________________________________________________________ END SORTING

    //controller will pick which search method to call based on inputs selected in the view and pass in the search term
    //method will return a List<> of listings or users or varieties


//_____________________________________________________________________________________________________ Generic GETTERS / SETTERS

    public List<User> getUsers(){
        return filteredUsers;
    }

    public void setFilteredUsersToAll(){
        filteredUsers = (List<User>) userRepository.findAll();
    }




}
