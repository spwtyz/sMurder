package me.spwtyz.murder.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

public class WeatherBlock implements Listener {
	
	  @EventHandler
	  public void onWeatherChange(WeatherChangeEvent evt) {
	    evt.setCancelled(evt.toWeatherState());
	  }
	  
		@EventHandler
		public void Food(FoodLevelChangeEvent evt) {
				evt.setCancelled(true);
			}
	    }
   

