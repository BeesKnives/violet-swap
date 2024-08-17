package com.launchcode.violetSwap.controllers;

import com.launchcode.violetSwap.models.*;
import com.launchcode.violetSwap.models.data.ListingRepository;
import com.launchcode.violetSwap.models.data.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.launchcode.violetSwap.models.data.VarietyRepository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

//connects to search/varieties, and search/variety/{id}
@Controller
@RequestMapping("/search")
public class SearchController {

    @Autowired
    private VarietyRepository varietyRepository;
    @Autowired
    private SearchService searchService; //instance of searchService, so we can call search methods
    @Autowired
    private ListingRepository listingRepository;
    @Autowired
    private UserRepository userRepository;
//______________________________________________________________________________________________SEARCH BY USER
//_______________________________________________________________________________________________________________

    @GetMapping("/users")//_________________________________________________Browse All Users
    public String searchUsers(Model model){
        searchService.setFilteredUsersToAll();
        model.addAttribute("users", (List<User>) userRepository.findAll());
        return "/search/users";
    }

    @PostMapping("/users")//
    public String processSearchUsers(Model model, @RequestParam(required=false) String searchTerm, @RequestParam(required=false) String sortBy, HttpServletRequest request){

        if (searchTerm != null && !searchTerm.isEmpty()) { //if searchTerm param is present
            List<User> userList = searchService.searchUsers(searchTerm); //set a list of users that match searchTerm in searchService
            model.addAttribute("searchFor", searchTerm);
            if (userList.isEmpty()){ //if userList is not present, return to search/users
                //todo: error msg: no users found
                return "redirect:/search/users";
            }
        }
//        else{ //if searchTerm is not present, set users to all in searchService
//            searchService.setFilteredUsersToAll();
//        }

        if (sortBy != null && !sortBy.isEmpty()){ //if sortBy param is present, sort users in searchService
            if (Objects.equals(sortBy, "distanceAscending")){
                searchService.sortUsersByDistance(request);
            } else if (Objects.equals(sortBy, "distanceDescending")){
                searchService.ReverseSortUsersByDistance(request);
            }
        }


        List<User> userList = searchService.getUsers(); //get userList from searchService and add it to the model
        model.addAttribute("users", userList);


        return "/search/users";


    }
//______________________________________________________________________________________________SEARCH BY VARIETY
//_______________________________________________________________________________________________________________
    @GetMapping("/varieties")//_________________________________________________Browse All Varieties and can search
    public String searchVarieties(Model model) {
        List<Variety> varieties = varietyRepository.findAll();
        model.addAttribute("varieties", varieties);
        return "/search/varieties";
    }


    @PostMapping("/varieties")//_____________________________________________________________search for a variety
    public String processSearchVarieties(Model model, @RequestParam String searchTerm) {

        if (searchTerm != null && !searchTerm.isEmpty()) { //if searchTerm is present

            List<Variety> varietyList = searchService.searchVarieties(searchTerm); //search for variety, returns a list of varieties that contain the search term(s)

            if (varietyList != null) { //if varietyList is present

                if (varietyList.size() == 1){//if there's only 1 variety in varietyList, redirect to search/variety w/ the id.
                    Variety singleVariety = varietyList.get(0);
                    return "redirect:/search/listings?varietyId=" + singleVariety.getId();

                }else{ //if there are multiple varieties in varietyList, add them to model, and return "search/varieties"

                    model.addAttribute("varieties",varietyList); //add varieties
                    return "/search/varieties";
                }
            }
            // if varietylist is not found, return to search/varieties
            return "redirect:/search/varieties";
        }
        //if varietySearch is null or empty, return to search/varieties
        return "redirect:/search/varieties";
    }

//__________________________________________________________________________________________________SEARCH BY LISTING
//_______________________________________________________________________________________________________________
    //todo: later - maybe instead of {id}, it's {variableName}?

    //____________________________________________________________________________________________________show listings


    @GetMapping("/listings") //listings?variety={varietyId}
    public String displayListings(@RequestParam(required=false) Integer varietyId, Model model, HttpServletRequest request) {
        HttpSession session = request.getSession();
        Integer userId = (Integer) session.getAttribute("user");


        List<Listing> listings = null;

        if (varietyId != null){ //if varietyId exists
            listings = searchService.setFilteredListingsByVariety(varietyId); // set listings that match the variety
        }else{
            listings = listingRepository.findAll(); //if no variety, set all listings in listings
        }

        model.addAttribute("userId", userId);
        model.addAttribute("listings", listings);

        return "search/listings";
    }

    @PostMapping("/listings")
    public String handleDisplaySortedListings(@RequestParam(required=false) Integer varietyId, Model model, HttpServletRequest request, @RequestParam String sortBy){
        HttpSession session = request.getSession();
        Integer userId = (Integer) session.getAttribute("user");
        model.addAttribute("userId", userId);


        if(varietyId == null){//if no variety being sent in:
            searchService.setFilteredListingsByVariety(null);
        }


        List<Listing> sortedListings = null;
        if (Objects.equals(sortBy, "distanceAscending")){
            sortedListings=searchService.sortListingsByDistance(request);
        } else if (Objects.equals(sortBy, "distanceDescending")){
            sortedListings=searchService.ReverseSortListingsByDistance(request);
        }
        model.addAttribute("listings", sortedListings);

        return "search/listings";
    }


}
