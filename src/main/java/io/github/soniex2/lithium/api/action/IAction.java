package io.github.soniex2.lithium.api.action;

/**
 * An energy action.
 *
 * @author soniex2
 */
public interface IAction {
	boolean commit();

	boolean revert();

	/**
	 * Returns the amount of energy in this action.
	 *
	 * @return The amount of energy in this action.
	 */
	int getEnergy();
}
