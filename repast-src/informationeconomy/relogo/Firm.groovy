/**
 * 
 */
package informationeconomy.relogo

import static repast.simphony.relogo.Utility.*;
import static repast.simphony.relogo.UtilityG.*;
import repast.simphony.relogo.Plural;
import repast.simphony.relogo.Stop;
import repast.simphony.relogo.Utility;
import repast.simphony.relogo.UtilityG;
import repast.simphony.relogo.schedule.Go;
import repast.simphony.relogo.schedule.Setup;
import informationeconomy.ReLogoTurtle;

/*
 * based on 
 */
class Firm extends ReLogoTurtle {

	def currentFood = 0
	def currentGold = 0
	
	def foodPerStep = 0
	def goldPerStep = 0
	
	def step() {
		def utilityMakingFood = utility(currentFood + foodPerStep, currentGold)
		def utilityMakingGold = utility(currentFood, currentGold + goldPerStep)
		if (utilityMakingFood > utilityMakingGold) {
			currentFood += foodPerStep
		} else {
			currentGold += goldPerStep
		}
	}
	
	def utility(food, gold) {
		food * gold
	}
	
}
