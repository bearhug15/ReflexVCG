package su.nsk.iae.reflex.preprocess;

import su.nsk.iae.reflex.ir.IrProgram;

/**
 * Runs the preprocessing passes of Preprocessing.tex in the order they depend on.
 *
 * <p>The order is not incidental:
 *
 * <ol>
 *   <li><b>Name mangling</b> first, so that every later stage can identify a variable by
 *       name alone. It also generates the per-access names of direct bindings, which the
 *       type environment needs.</li>
 *   <li><b>Cast insertion</b> next, because it needs a type for every name, and because
 *       normalisation copies expressions - doing it after would mean copying untyped
 *       trees and typing each copy separately.</li>
 *   <li><b>Normalisation</b> last, so the states it synthesises already contain fully
 *       typed, explicitly converted expressions.</li>
 * </ol>
 *
 * <p>The result is a canonical program: names are globally unique, every conversion is
 * an explicit cast, and no {@code wait} or {@code slice} remains.
 */
public final class Preprocessor {

    private Preprocessor() {
    }

    /**
     * Rewrites {@code program} in place.
     *
     * @return the type environment built along the way, for stages that follow
     */
    public static TypeEnvironment run(IrProgram program) {
        NameManglingPass mangling = new NameManglingPass();
        mangling.run(program);

        TypeEnvironment types = new TypeEnvironment(program, mangling.getDirectAccessNames());
        new CastInsertionPass(types).run(program);

        new NormalizationPass().run(program);
        return types;
    }
}
