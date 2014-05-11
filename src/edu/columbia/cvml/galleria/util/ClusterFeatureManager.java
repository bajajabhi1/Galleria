package edu.columbia.cvml.galleria.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.util.Log;

public class ClusterFeatureManager 
{
	Context ctx = null;
	private static final String FEATURE_FILE = "clustering_file";
	private static final String IMAGE_FEATURE_MAP_FILE = "image_feature_map";
	private static final String LOG_TAG = "ClusterFeatureManager";
	private Map<String,Integer> imageFeatureMap = new HashMap<String,Integer>();
	public static final char FEATURE_SEPARATOR = ','; 
	
	int lineCounter = 0;
	
	public ClusterFeatureManager(Context context)
	{
		this.ctx = context;
		init();
	}
	
	public void init()
	{
		// Initialize the file only if there is no existing
		if (null == FileOperation.readFileFromInternalStorage(ctx, FEATURE_FILE))
		{
			FileOperation.writeFileToInternalStorage(ctx, FEATURE_FILE, HEADING + "\n");
			lineCounter = 0;
			Log.d(LOG_TAG,"Initialized new feature file");
		}
	}

	/**
	 *  Input is image file name and the string of annotations separated by separator
	 */
	public void addImageEntry(String imageName, String features)
	{
		Log.d(LOG_TAG," in addImageEntry");
		lineCounter++;
		imageFeatureMap.put(imageName,lineCounter);
		FileOperation.writeFileToInternalStorage(ctx, FEATURE_FILE, features + "\n");
	}
	
	public String loadClusterImageFile()
	{
		return FileOperation.readFileFromInternalStorage(ctx, FEATURE_FILE);
	}

	public Map<String,Integer> loadImageFeatureMap()
	{
		ObjectInputStream inputStream = null;
		try
		{
			inputStream = new ObjectInputStream(ctx.openFileInput(IMAGE_FEATURE_MAP_FILE));
			imageFeatureMap = (HashMap<String,Integer>)inputStream.readObject();         
			Log.d(LOG_TAG,"Reading from the file");
			Set<String> keys = imageFeatureMap.keySet();
			for(String key : keys)
			{
				Log.d(LOG_TAG,key + " => " + imageFeatureMap.get(key));
			}
		} catch (Exception e) {
			Log.e(LOG_TAG,e.getMessage());
			e.printStackTrace();
		}
		finally
		{
			if(inputStream!=null)
			{
				try {
					inputStream.close();
				} catch (IOException e) {
					//ignore
				}
			}
		}
		return imageFeatureMap;
	}

	public void writeImageFeatureMap()
	{
		ObjectOutputStream outStream = null;
		try
		{
			outStream = new ObjectOutputStream(ctx.openFileOutput(IMAGE_FEATURE_MAP_FILE,Context.MODE_PRIVATE));
			outStream.writeObject(imageFeatureMap);
			outStream.flush();
			outStream.close();
			Log.d(LOG_TAG,"Index written to file");
		} 
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}  
		finally
		{
			if(outStream!=null)
			{
				try {
					outStream.close();
				} catch (IOException e) {
					//ignore
				}
			}
		}

	}
	
	
	public static final String HEADING = "cloudy_moon,super_moon,misty_woods,cloudy_mountains,misty_field,"
			+ "cloudy_landscape,misty_road,tiny_bathroom,stunning_sunset,cloudy_valley,awesome_clouds,empty_train,"
			+ "misty_sunrise,rough_waves,classic_cars,bright_moon,tiny_mushrooms,powerful_waves,misty_night,lovely_clouds,"
			+ "cloudy_sunrise,crazy_clouds,calm_lake,bright_angel,rough_coast,golden_sunrise,clear_night,calm_ocean,"
			+ "lonely_boat,amazing_armory,cloudy_night,sunny_flowers,misty_trees,incredible_sunset,colorful_sky,super_cars,"
			+ "beautiful_rose,beautiful_butterfly,amazing_sunset,empty_office,clean_baby,great_sunset,sunny_trees,calm_beach,"
			+ "tiny_car,cloudy_view,gentle_river,ugly_building,curious_deer,quiet_study,rough_sketch,empty_desk,magical_sunset,"
			+ "beautiful_sunset,clear_sea,stormy_mountain,wet_grass,awesome_sunset,amazing_clouds,clear_moon,nice_beach,"
			+ "crowded_beach,stunning_clouds,magnificent_clouds,misty_morning,stormy_field,awesome_cars,misty_forest,peaceful_lake,"
			+ "cloudy_evening,cloudy_lake,empty_market,rainy_windows,incredible_sunrise,colorful_sunset,dark_street,wild_flowers,"
			+ "shiny_cars,tiny_fly,magnificent_mountain,incredible_beach,expensive_car,shiny_toy,clear_beach,young_pony,"
			+ "colorful_clouds,colorful_flowers,beautiful_clouds,great_ocean,stormy_clouds,prickly_heart,dead_fly,magnificent_church,"
			+ "lovely_beach,misty_autumn,stunning_waterfall,misty_lake,charming_house,bright_sunset,beautiful_sky,silly_face,"
			+ "clear_river,ugly_fish,wet_leaves,empty_street,smooth_waterfall,famous_church,empty_theatre,empty_airport,damaged_car,"
			+ "strange_clouds,colorful_street,icy_grass,ugly_drawing,warm_chocolate,nice_street,colorful_building,dark_tree,"
			+ "crowded_street,dirty_car,famous_lighthouse,tranquil_pond,empty_room,powerful_waterfall,sexy_halloween,"
			+ "expensive_house,young_deer,dry_landscape,misty_valley,nice_clouds,magnificent_sky,lost_lake,dry_field,ancient_bridge,"
			+ "clean_car,weird_building,dry_lake,icy_lake,golden_sunset,sleepy_cat,rainy_night,tiny_spider,ugly_car,fresh_leaves,"
			+ "icy_river,cloudy_city,funny_sign,great_sky,calm_sea,weird_bug,scenic_mountain,stormy_waves,tiny_bug,quiet_lake,"
			+ "wild_deer,magical_sky,rotten_tree,fantastic_car,ancient_trees,classic_sports,misty_winter,traditional_fishing,"
			+ "smooth_sand,dry_forest,nice_building,quiet_pond,empty_field,amazing_architecture,fantastic_beach,sunny_field,"
			+ "amazing_sky,natural_hair,golden_autumn,tiny_insect,smooth_waves,clear_ocean,heavy_clouds,quiet_park,colorful_autumn,"
			+ "misty_hills,calm_river,stormy_landscape,abandoned_hospitals,weird_clouds,cute_puppy,sleepy_puppy,wild_flora,"
			+ "pretty_sky,damaged_building,classic_commercial,comfortable_room,tranquil_lake,charming_street,smooth_clouds,"
			+ "beautiful_beach,wild_grass,peaceful_creek,fluffy_plant,colorful_trees,empty_bathroom,gentle_waves,creepy_forest,"
			+ "busy_river,clear_mountain,fantastic_flowers,incredible_sky,clear_pool,calm_waves,classic_rally,smooth_sea,"
			+ "wild_mushrooms,famous_building,busy_street,dry_island,fluffy_snow,magnificent_view,muddy_legs,tiny_hands,"
			+ "weird_plant,busy_city,calm_sky,quiet_creek,colorful_leaves,tired_dad,lonely_island,hot_car,classic_architecture,"
			+ "cloudy_morning,wet_cat,beautiful_flower,broken_window,expensive_boat,magical_forest,hot_pool,empty_building,"
			+ "traditional_tattoo,fantastic_architecture,dangerous_road,weird_tree,fluffy_clouds,dark_forest,creepy_tree,"
			+ "crazy_crowd,rainy_market,fantastic_sunrise,smiling_baby,young_tree,funny_baby,fancy_car,lonely_road,sleepy_baby,"
			+ "ugly_fly,gorgeous_butterfly,dirty_snow,lovely_church,stupid_sign,clean_sea,fresh_baby,icy_forest,dark_clouds,"
			+ "weird_fog,dying_tree,incredible_mountain,clear_road,wicked_storm,upset_kids,clean_room,famous_hotel,gorgeous_building,"
			+ "happy_baby,cheerful_flowers,ancient_forest,sunny_lake,chubby_face,rainy_landscape,wet_window,cold_room,"
			+ "traditional_house,mad_face,shiny_hair,scary_forest,drunk_girls,lovely_street,scary_tree,outdoor_pool,tiny_flower,"
			+ "holy_island,icy_fence,heavy_snow,precious_baby,dead_bat,pretty_tree,peaceful_sleep,haunted_woods,icy_tree,bad_sign,"
			+ "scenic_road,tranquil_river,derelict_factory,colorful_fish,colorful_butterfly,super_hotel,broken_ice,relaxing_beach,"
			+ "lonely_mountain,wild_horse,rough_road,wet_sand,amazing_cars,cute_baby,quiet_street,famous_monument,fresh_grass,"
			+ "freezing_fog,little_church,smiling_eyes,gorgeous_eyes,favorite_painting,calm_water,strange_sign,stunning_architecture,"
			+ "amazing_trees,quiet_sea,sleepy_dog,fat_loss,stunning_mountain,dead_snake,clear_water,graceful_bird,scenic_river,"
			+ "golden_leaves,famous_tower,clear_morning,fat_fly,laughing_baby,wild_garden,lost_places,dead_tree,ancient_church,"
			+ "rainy_forest,stormy_coast,rainy_street,dry_tree,damaged_house,awesome_flowers,chubby_baby,smiling_dog,classic_race,"
			+ "famous_castle,hardcore_punk,beautiful_eyes,gorgeous_dress,peaceful_ocean,shy_animals,rainy_clouds,tired_cat,bad_road,"
			+ "abandoned_boat,extreme_noise,warm_sand,cloudy_winter,sparkling_eyes,amazing_cake,strange_tree,wild_party,lost_cat,"
			+ "abandoned_house,ancient_fortress,damaged_home,dead_skull,traditional_architecture,dark_woods,clean_pool,hardcore_band,"
			+ "abandoned_cemetery,scary_storm,natural_lake,sexy_girls,dying_flower,abandoned_vehicle,attractive_flowers,"
			+ "incredible_view,fresh_snow,wet_hair,beautiful_autumn,smooth_skin,outstanding_view,dark_places,sweet_face,"
			+ "awesome_view,misty_mountains,great_hall,ancient_monument,scary_bug,pretty_kitty,ugly_spider,prickly_flower,"
			+ "natural_bridge,wild_coast,scenic_coast,charming_church,cloudy_forest,playful_dog,lost_pets,anxious_crowd,"
			+ "beautiful_blossom,victorious_team,natural_pool,fancy_hair,ugly_face,stunning_flower,sunny_road,crazy_car,lonely_street,dirty_shoes,colorful_food,sweet_chocolate,sweet_puppy,calm_bay,traditional_boat,tranquil_garden,derelict_house,wet_dog,dry_grass,ancient_castle,bright_city,friendly_dog,sleepy_face,fancy_shoes,cold_night,cute_cat,muddy_dog,tired_puppy,wet_woods,damaged_road,silly_drawing,rainy_lake,dry_sand,melted_chocolate,scary_spider,rotten_wood,bright_autumn,wild_rose,wet_road,lovely_butterfly,busy_bridge,ugly_doll,adorable_cat,fantastic_wildlife,abandoned_factory,derelict_building,muddy_girls,tranquil_water,peaceful_park,haunted_building,powerful_cars,stunning_building,abandoned_train,lovely_autumn,hungry_crowd,young_birds,bad_graffiti,dangerous_building,gentle_eyes,weird_food,natural_mirror,empty_boat,strong_trees,innocent_smile,pretty_rose,tiny_book,filthy_car,sleepy_kitty,fresh_tattoo,excited_crowd,sexy_dress,noisy_toys,dusty_road,playful_cats,bright_sky,stunning_view,classic_castle,peaceful_boat,sleepy_kids,wild_cat,classic_rose,"
			+ "fascinating_flower,cute_kitty,proud_artist,misty_river,tiny_boat,christian_church,natural_spring,fluffy_dog,ugly_feet,awesome_blossom,serene_reflections,famous_beach,friendly_cat,abandoned_building,lovely_garden,stormy_river,lost_coast,haunted_forest,creepy_spider,incredible_architecture,smooth_water,bright_lights,wet_snow,nice_letter,slender_trees,abandoned_hotel,quiet_river,"
			+ "falling_star,strange_fly,golden_pond,dead_horse,lonely_train,comfortable_bed,famous_car,favorite_flower,awesome_architecture,ancient_building,pretty_petals,"
			+ "funny_card,clean_windows,crazy_fire,favorite_architecture,dusty_sky,hot_girls,golden_statue,outdoor_market,dying_rose,hungry_baby,abandoned_car,beautiful_garden,helping_student,abandoned_school,classic_dance,cloudy_bay,fat_spider,pretty_eyes,abandoned_asylum,cute_girls,stormy_night,happy_dog,strange_insect,derelict_boat,dirty_desk,icy_road,funny_dog,wet_forest,amazing_flowers,quiet_bay,shiny_moon,rough_mountain,crowded_city,magical_castle,elegant_architecture,classic_home,"
			+ "icy_fog,fluffy_hair,fresh_rose,silly_baby,magnificent_scenery,lovely_city,strange_building,crazy_storm,dry_leaves,fluffy_grass,broken_car,yummy_cake,magnificent_castle,lazy_cat,grieving_parents,favorite_street,haunted_cemetery,greasy_food,derelict_farm,insane_asylum,dirty_glass,christian_concert,magnificent_flowers,weird_graffiti,beautiful_paintings,colorful_cake,cuddly_cat,fancy_dress,fluffy_puppy,attractive_birds,wet_evening,damaged_church,icy_snow,traditional_dance,colorful_garden,dirty_bathroom,busy_crowd,dirty_wall,bad_storm,young_driver,heavy_train,creepy_cemetery,scary_halloween,golden_bay,natural_beach,fancy_food,stunning_landscape,misty_rain,lonely_car,pleasant_park,sexy_shoes,rough_landscape,calm_street,dead_insect,bright_rainbow,cold_mountains,lost_teeth,super_party,falling_snow,dead_leaves,empty_space,haunted_graveyard,expensive_hotel,splendid_church,little_boat,favorite_comics,scenic_cruise,sunny_garden,awesome_shoes,violent_sea,dirty_dog,outdoor_cinema,broken_ipod,great_landscape,famous_street,"
			+ "healthy_hair,lonely_grave,sexy_lips,busy_train,traditional_food,damaged_window,"
			+ "heavy_rain,adorable_dog,contagious_smile,playful_animal,crying_baby,damaged_fence,innocent_eyes,wild_water,magnificent_garden,favorite_city,colorful_lights,bad_accident,broken_piano,awesome_cake,awesome_lego,abandoned_graveyard,empty_chair,sexy_model,sweet_smile,abandoned_places,inspirational_poster,outdoor_training,strange_spider,"
			+ "rough_skin,nasty_spider,dirty_toilet,stunning_tree,sexy_hair,dangerous_snake,tasty_cake,gross_bugs,crazy_graffiti,pretty_flowers,quiet_forest,broken_tree,curious_cat,ancient_street,lonely_night,lonely_house,natural_history,sunny_valley,pretty_lights,silly_hat,fluffy_cat,calm_autumn,golden_flower,sad_dog,sad_eyes,yummy_pie,super_team,strange_flower,tame_bird,famous_painting,cute_dog,lovely_view,delightful_flower,clean_beach,calm_pond,funny_cat,colorful_bird,bright_eyes,clean_face,lost_river,abandoned_office,rainy_city,healthy_plant,beautiful_smile,sunny_sky,dark_room,funerary_monument,elegant_dress,"
			+ "awesome_tattoo,ugly_bug,scenic_ocean,dying_plant,tiny_feet,empty_pool,little_island,stunning_eyes,relaxing_view,grumpy_baby,crying_angel,lovely_card,golden_evening,hot_drink,sweet_lips,colorful_eggs,pretty_view,violent_waves,crazy_hat,angry_cat,funerary_statue,scenic_garden,sexy_teen,clean_river,quiet_winter,traditional_wedding,magnificent_architecture,evil_cat,freezing_mist,pretty_hair,creepy_house,smooth_lake,ancient_sculpture,famous_statue,healthy_baby,adorable_doll,noisy_bird,powerful_ocean,dirty_feet,delicious_food,weird_face,beautiful_landscape,crazy_face,outdoor_party,shiny_eyes,weird_insect,bad_car,elegant_wedding,empty_house,heavy_storm,empty_glass,dark_fortress,delicious_pie,creepy_bug,tough_face,holy_child,broken_egg,"
			+ "haunted_tower,magnificent_sunrise,peaceful_places,gorgeous_morning,fascinating_architecture,tranquil_scene,shy_smile,adorable_face,peaceful_winter,dark_eyes,clear_autumn,calm_pool,adorable_puppy,dry_flower,laughing_eyes,strange_house,gorgeous_model,stunning_garden,cute_cake,gorgeous_card,scenic_reserve,cuddly_kittens,favorite_band,abandoned_construction,excellent_food,tiny_house,bloody_eyes,fragile_flower,ugly_wall,sunny_autumn,traditional_farm,lovely_home,sweet_cake,dead_spider,tired_baby,gorgeous_baby,crying_face,sweet_cupcake,stunning_smile,nasty_bathroom,excited_face,energetic_performance,serene_sky,creepy_shadow,adorable_dress,outdoor_plant,young_beauty,cuddly_baby,natural_garden,serene_lake,grumpy_cat,fascinating_landscape,lonely_chair,delicious_meat,scared_face,sick_eyes,broken_fence,great_food,natural_stream,abandoned_tank,stupid_face,tiny_cage,worried_face,sick_cat,stupid_hat,tranquil_morning,fluffy_bed,cuddly_dog,sexy_blonde,serene_scene,warm_creek,shiny_apple,fluffy_kitty,elegant_rose,adorable_smile,sexy_heels,dirty_cat,natural_sculpture,lovely_smile,funny_hair,traditional_market,"
			+ "proud_parents,yummy_meat,comfortable_chair,busy_office,beautiful_night,sexy_legs,little_flower,peaceful_scene,proud_student,gross_food,illegal_war,wet_area,magical_view,ancient_statue,curious_puppy,cute_shoes,delicious_cake,aggressive_dog,attractive_face,harsh_landscape,smiling_horse,great_night,stunning_dress,precious_angel,splendid_scenery,ugly_cat,evil_queen,fascinating_building,broken_chair,healthy_food,dumb_cat,amazing_view,amazing_food,mad_king,dry_wall,grotesque_head,broken_glass,warm_pool,gentle_smile,hungry_pig,playful_puppy,nice_autumn,little_puppy,tired_eyes,holy_church,awesome_food,great_view,pretty_face,delicious_chocolate,dark_winter,great_reflection,lost_shoes,empty_box,warm_lights,tiny_raindrops,jolly_santa,"
			+ "clean_kitty,bad_phone,golden_hair,scary_house,pretty_angel,classic_cocktail,amazing_paintings,gorgeous_autumn,fresh_flowers,"
			+ "peaceful_morning,clean_shoes,hot_cup,safe_car,ugly_fence,tired_dog,clean_teeth,super_festival,dumb_face,serene_garden,magical_flowers,fancy_party,hot_body,traditional_celebration,outdoor_festival,yummy_food,quiet_night,attractive_male,"
			+ "traditional_church,pretty_scene,slippery_road,muddy_river,angry_face,innocent_face,little_beauty,bright_face,sick_face,weird_sculpture,hungry_puppy,dangerous_spider,favorite_food,ancient_architecture,gorgeous_dolls,warm_bath,powerful_wings,hot_model,outdoor_concert,amazing_city,tired_girls,funny_wedding,hungry_zombie,dead_bug,pretty_shoes,excited_student,handsome_face,wild_bird,sexy_smile,silly_cat,dusty_mirror,wary_birds,sweet_princess,happy_cats,dark_night,delicious_apple,scary_face,scared_dog,tasty_food,charming_places,elegant_bird,cuddly_puppy,classic_coke,gorgeous_scene,beautiful_girl,smiling_girls,shy_cat,ancient_farm,sexy_chest,nice_cup,beautiful_city,amazing_hair,dead_fish,evil_skull,excellent_architecture,powerful_river,wild_animals,rotten_apple,amazing_race,stupid_cat,outdoor_performance,pretty_dress,sad_cat,christian_wedding,hot_food,clear_lake,great_street,haunted_attraction,young_lady,quiet_dawn,damaged_wall,natural_food,curious_bird,laughing_girls,colorful_landscape,relaxing_cruise,"
			+ "bad_view,creepy_eyes,broken_phone,cute_face,graceful_animals,little_pony,cute_animals,sexy_body,bloody_wall,friendly_children,silly_smile,hot_heels,safe_driver,proud_flag,shiny_shoes,lonely_piano,excited_kids,tranquil_park,wild_hair,derelict_ship,funny_face,hot_legs,gentle_flowers,slippery_snow,dead_terrorist,bloody_lips,angry_eyes,shiny_city,traditional_mask,"
			+ "quiet_baby,incredible_eyes,outdoor_wedding,rough_hands,christian_cross,rainy_river,cold_darkness,funny_glasses,gorgeous_hair,natural_wonder,broken_wall,cute_smile,bright_sea,fantastic_landscape,abandoned_industry,evil_shadow,warm_hat,dirty_streets,yummy_cupcake,cloudy_autumn,clean_bed,scary_snake,stormy_autumn,beautiful_view,stupid_girls,tired_feet,outstanding_architecture,pretty_baby,angry_bull,playful_kitten,bright_street,clean_hair,excellent_view,clear_sky,incredible_animal,fragile_wings,awesome_hair,graceful_tree,classic_princess,dark_wall,cute_dress,tiny_dog,lazy_girls,healthy_skin,favorite_team,traditional_christmas,cute_butt,magnificent_sea,faithful_dog,bloody_snow,crazy_hair,excellent_scenery,bad_hair,little_house,tiny_backyard,nice_scene,icy_winter,colorful_dress,mad_cat,mad_hair,funny_cake,little_doll,young_fan,bright_snow,famous_bridge,golden_cross,smelly_flowers,expensive_sports,quaint_city,broken_wings,favorite_dress,derelict_car,handsome_dad,golden_sunlight,dirty_face,little_baby,angry_men,adorable_baby,weird_war,classic_design,strong_men,"
			+ "bad_cat,excellent_book,powerful_animal,cute_toy,super_food,nice_heels,great_adventure,clear_skin,nasty_feet,sexy_tattoo,crying_statue,dark_skin,outdoor_lights,hilarious_guy,wild_waves,haunted_hotel,happy_clown,shy_dog,famous_landmark,"
			+ "smooth_curves,natural_reserve,crowded_bridge,famous_architecture,dry_food,traditional_festival,bright_sun,crying_eyes,tiny_doll,sad_statue,tired_face,peaceful_mountain,dead_cockroach,traditional_home,quiet_scene,ancient_city,falling_leaves,clean_house,lonely_dog,dry_river,magnificent_city,gorgeous_winter,young_friends,adorable_girls,cute_dolls,beautiful_earth,derelict_hospitals,crying_girls,holy_city,crazy_horse,favorite_park,jolly_christmas,silly_dog,fascinating_city,colorful_spring,lonely_city,pretty_present,awesome_birds,grumpy_face,chubby_cat,golden_sun,funerary_architecture,fancy_garden,strong_beer,proud_father,fresh_meat,silly_kitty,dirty_window,happy_mother,traditional_dress,hilarious_face,crazy_zombie,friendly_smile,shiny_lips,awesome_night,tasty_pie,handsome_smile,pensive_face,cute_bird,hot_blonde,silly_girls,strong_hands";


}
