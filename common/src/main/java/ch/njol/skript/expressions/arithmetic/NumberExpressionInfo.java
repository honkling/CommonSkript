/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.expressions.arithmetic;

import ch.njol.skript.lang.Expression;
import org.bukkit.event.Event;

@Deprecated
public class NumberExpressionInfo implements ArithmeticGettable<Number> {

	private final Expression<? extends Number> expression;

	public NumberExpressionInfo(Expression<? extends Number> expression) {
		this.expression = expression;
	}

	public Number get(Event event, boolean integer) {
		return get(event);
	}

	@Override
	public Number get(Event event) {
		Number number = expression.getSingle(event);
		return number != null ? number : 0;
	}

	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}
}
