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

	def initialFood = 0
	def initialGold = 0
	
	def currentFood = 0
	def currentGold = 0
	
	def actions = []
	def activity = []
	
	def foodPerStep = 0
	def goldPerStep = 0
	
	def step() {
		def utilityMakingFood = utility(currentFood + foodPerStep, currentGold)
		def utilityMakingGold = utility(currentFood, currentGold + goldPerStep)

		// work out the mrs for the firm
		def mrs = mrs()

		// set up a variable to record the best trade found
		def trade = [
			firm: null,
			price: 0,
			food: currentFood,
			gold: currentGold,
			utility: currentUtility()
		]

		// cycle through each of the firms to see whether a trade is worthwhile
		def thisFirm = self()
		firms().each {
			def result = null
			if (mrs >= 1 && it.mrs() < 1) {
				// more GOLD than FOOD, so buy FOOD
				result = thisFirm.tryBuyingFood(it)
			} else if (mrs < 1 && it.mrs() >= 1) {
				// more FOOD than GOLD, so sell FOOD
				result = thisFirm.trySellingFood(it)
			} else {
				result = trade
			}
			// set the best trade to the result if it's a better trade than the best found so far
			if (result['firm'] == null) {
				trade = result
			} else if (result['utility'] > trade['utility']) {
				trade = result
			}
		}
		
		
		def action = []
		def randomlyTrue = random(10000) > 5000
		if (utilityMakingFood > utilityMakingGold || (utilityMakingFood == utilityMakingGold && randomlyTrue)) {
			randomlyTrue = random(10000) > 5000
			if (trade['utility'] > utilityMakingFood || (trade['utility'] == utilityMakingFood && randomlyTrue)) {
				action = makeTrade(trade)
			} else {
				currentFood += foodPerStep
				action = [ type: 'make', good: 'food', amount: foodPerStep, utility: currentUtility() ]
			}
		} else if (trade['utility'] > utilityMakingGold || (trade['utility'] == utilityMakingGold && randomlyTrue)) {
			action = makeTrade(trade)
		} else {
			currentGold += goldPerStep
			action = [ type: 'make', good: 'gold', amount: goldPerStep, utility: currentUtility() ]
		}
		actions << action
		activity << action
	}
	
	def utility(food, gold) {
		food * gold
	}

	def currentUtility() {
		utility(currentFood, currentGold)
	}
	
	def mrs() {
		currentGold / currentFood
	}
	
	def priceForTrade(firm) {
		(currentGold + firm.currentGold) / (currentFood + firm.currentFood)
	}
	
	def tryBuyingFood(firm) {
		def price = priceForTrade(firm)
		def foodToBuy = Math.floor((firm.currentFood - currentFood) / 2)
		def result = [
			firm: firm,
			price: price,
			food: currentFood + foodToBuy,
			gold: currentGold - foodToBuy * price,
			utility: 0
		]
		result.put('utility', utility(result['food'], result['gold']))
		return result
	}
	
	def trySellingFood(firm) {
		def price = priceForTrade(firm)
		def foodToSell = Math.floor((currentFood - firm.currentFood) / 2)
		def result = [
			firm: firm,
			price: price,
			food: currentFood - foodToSell,
			gold: currentGold + foodToSell * price,
			utility: 0
		]
		result.put('utility', utility(result['food'], result['gold']))
		return result
	}

	def makeTrade(trade) {
		def action = [ type: 'trade', food: trade['food'] - currentFood, gold: trade['gold'] - currentGold, price: trade['price'], utility: trade['utility'] ]
		trade['firm'].currentFood += currentFood - trade['food']
		trade['firm'].currentGold += currentGold - trade['gold']
		trade['firm'].activity << [ type: 'receive-trade', food: currentFood - trade['food'], gold: currentGold - trade['gold'], price: trade['price'], utility: trade['firm'].currentUtility() ]
		currentFood = trade['food']
		currentGold = trade['gold']
		return action
	}
	
}
