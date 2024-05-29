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

        //user.getLatitude() is getting null. user and user.getId() are working fine though. Check MySQL
        //todo: when a user is made with oauth, it's not automatically adding on the latitude/longitude. make sure that extra page where you add zipcode and email calls ZipcodeDataService and does the thing.


        Double userLatitude = user.getLatitude(); //get lat/long of user
        System.out.println(userLatitude + "_______________________________________________________________");
        Double userLongitude = user.getLongitude();


        //get list of users in listings??


        for ( Listing listing : filteredListings ) { //for each listing in filteredListings
            System.out.println("user: " + listing.getUser());
            System.out.println("current distance: " + listing.getUser().getDistance());
            if(listing.getUser().getDistance()==(null)){ //check if the listing's user's distance has already been calculated.
                //calculate distance between user and the other users:
                Double listingLatitude = listing.getUser().getLatitude(); //get lat/long of listing
                Double listingLongitude = listing.getUser().getLongitude();

                Double distanceLatitude = Math.abs(userLatitude - listingLatitude); //get distance between lat/long
                Double distanceLongitude = Math.abs(userLongitude - listingLongitude);

                Double distance = (distanceLatitude + distanceLongitude) / 2; //calculate overall distance

                listing.getUser().setDistance(distance); //set distance in User
            }
            System.out.println("calculated distance: " + listing.getUser().getDistance() + "\n");
        }
        //once distance between user and other users has been calculated,

        //order the listings by listing.getUser().getDistance() - done in Listing's compareTo override, implemented here
        Collections.sort(filteredListings);


        //TODO: this is quite clunky, we should look into using a SQL query to order the list of listings, then delete those that don't match filteredlistings?
        //todo: no. Send the list of listing ids in the SQL query, have it pick them out and order them, and then send the data back here??



        //loop through each listing
            //check - if listing.user has relativeDistance already on it, break
        //get listing.user.latitude and listing.user.longitude
        //do math to find the distance between the two (look up? minus each and convert any negatives to positives? Then add them together and divide by 2)
        //set a relative distance field in user?
            //todo:have a check, if user id matches previously calculated users, just use the already made relative distance filed
            //(maybe?) check - list of user's listings and list of filtered listings, if they match, put user's relative distance on them.
        //sort by relative distance (maybe have a separate method that can switch ascending/descending by reversing the order of sorting)



        return filteredListings;
    }

    //sort listings by distance descending
    public List<Listing> ReverseSortListingsByDistance(HttpServletRequest request) {

        sortListingsByDistance(request);
        Collections.reverse(filteredListings);

        return filteredListings;
    }

//_____________________________________________________________________________________________________ END SORTING

    //controller will pick which search method to call based on inputs selected in the view and pass in the search term
    //method will return a List<> of listings or users or varieties

}
