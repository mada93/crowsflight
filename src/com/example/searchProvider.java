package com.example;

import android.content.SearchRecentSuggestionsProvider;


public class searchProvider extends SearchRecentSuggestionsProvider {
	searchProvider(){
			setupSuggestions("searchProvider", 0);
	}



	
}