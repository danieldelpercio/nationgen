package nationGen.misc;

import java.util.ArrayList;
import java.util.List;

public class CommandSignature {
    List<List<ArgType>> signature = new ArrayList<>();

    public CommandSignature defineNewArg(ArgType... possibleTypes) {
        boolean isRequiredArg = !List.of(possibleTypes).contains(ArgType.OPTIONAL);

        if (possibleTypes.length == 0) {
            throw new IllegalArgumentException("Cannot add new argument to signature without any possible type");
        }

        if (isRequiredArg && this.isLastArgOptional()) {
            throw new IllegalArgumentException("Cannot add required argument after an optional argument");
        }

        this.signature.add(List.of(possibleTypes));
        return this;
    }

    public boolean acceptsArgType(ArgType argType) {
        return this.signature.stream().anyMatch(possibleArgTypes -> {
            return possibleArgTypes.contains(argType);
        });
    }

    public boolean acceptsArgTypeAt(ArgType argType, int argIndex) {
        if (this.signature.size() <= argIndex) {
            return false;
        }

        return this.signature.get(argIndex).contains(argType);
    }

    private boolean isLastArgOptional() {
        if (this.signature.isEmpty()) {
            return false;
        }

        return this.signature.getLast().contains(ArgType.OPTIONAL);
    }

    public static CommandSignature empty() {
        return new CommandSignature();
    }
}
