package com.launchcode.violetSwap.controllers;

import com.launchcode.violetSwap.models.Listing;
import com.launchcode.violetSwap.models.Maturity;
import com.launchcode.violetSwap.models.SearchService;
import com.launchcode.violetSwap.models.Variety;
import com.launchcode.violetSwap.models.data.ListingRepository;
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

//______________________________________________________________________________________________SEARCH BY VARIETY
//_______________________________________________________________________________________________________________


    @GetMapping("/varieties")//_________________________________________________Browse All Varieties and can search
    public String searchVarieties(Model model) {

        //optional searchParam/query in url?
        //if searchParam is present, do the thing

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
                    return "redirect:/search/variety/" + singleVariety.getId();

                }else{ //if there are multiple varieties in varietyList, add them to model, and return "search/varieties"
//                    model = null; //reset model?? nope, bad. Model can't be null
                    //model.clear(); doesn't work :(
                    //how to clear the model so that it doesn't keep ahold of the previous request's varieties??

                    //HttpSession session = request.getSession();
                    //session.removeAttribute("varieties");

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

    //todo: varietyId passed in the url from (POST)/varieties to (GET)/listings ((and (POST)/listings?) - test if needed) (getting rid of (GET & POST)variety/{id})
    // controller takes the id and sets the list in searchservice, if no id, set list to all


    //todo: later - maybe instead of {id}, it's {variableName}?

    //____________________________________________________________________________________________________show listings

    @GetMapping("/variety/{id}")//_________________________________________________Show Listings in selected Variety
    public String showListingsForVariety(@PathVariable Integer id, Model model) {

        //todo: set PathVariable to optional, if null, show all listings

        Variety selectedVariety = varietyRepository.findById(id).orElse(null);
        if (selectedVariety != null) {
            model.addAttribute("listings", selectedVariety.getListings());
            model.addAttribute("selectedVariety", selectedVariety);
            return "/search/listings";
        } else {
            return "redirect:/search/varieties";
        }
    }



    @PostMapping("/variety/{id}") //______________________________________________Sort Listings in selected Variety
    public String searchListingsForVariety(@PathVariable Integer id, Model model, HttpServletRequest request, @RequestParam String sortBy){
        HttpSession session = request.getSession();
        Integer userId = (Integer) session.getAttribute("user");
        model.addAttribute("userId", userId);

        List<Listing> listings = searchService.setFilteredListingsByVariety(id); //gets listings of this variety, and sets


        //copy/paste from below:
        List<Listing> sortedListings = null;
        if (Objects.equals(sortBy, "distanceAscending")){
            sortedListings=searchService.sortListingsByDistance(request);
        } else if (Objects.equals(sortBy, "distanceDescending")){
            sortedListings=searchService.ReverseSortListingsByDistance(request);
        }
        model.addAttribute("listings", sortedListings);


        return "/search/listings";
        //set searchService's Listings to listingRepository.findById(), then send to listings PostMapping
    }


    @GetMapping("/listings") //todo: maybe have this as /listings/{id}, and have the id be all or 0?? for the non-specific searches.
    public String displayListings(Model model, HttpServletRequest request) {
        HttpSession session = request.getSession();
        Integer userId = (Integer) session.getAttribute("user");

        List<Listing> listings = listingRepository.findAll();
//
//        List<Listing> listings;
//        if(id==null){
//            listings = listingRepository.findAll(); //if no selected variety, continue as normal
//        }else{
//
//            Variety selectedVariety = varietyRepository.findById(id).orElse(null);
//            if (selectedVariety != null) {
//                listings = (List<Listing>) selectedVariety.getListings(); //get listings for selected variety
//            }else {
//                return "redirect:/search/varieties";
//            }
//        }


        model.addAttribute("userId", userId);
        model.addAttribute("listings", listings);

        return "search/listings";
    }

    @PostMapping("/listings")
    public String handleDisplaySortedListings(Model model, HttpServletRequest request, @RequestParam String sortBy){
        HttpSession session = request.getSession();
        Integer userId = (Integer) session.getAttribute("user");
        model.addAttribute("userId", userId);

//        if (user == null){
//            return "redirect:/user/update";
//        }
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
