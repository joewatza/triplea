package games.strategy.internal.persistence.serializable;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;

import javax.annotation.concurrent.Immutable;

import games.strategy.engine.data.IAttachment;
import games.strategy.persistence.serializable.Proxy;
import games.strategy.persistence.serializable.ProxyFactory;
import games.strategy.triplea.delegate.FakeTechAdvance;

/**
 * A serializable proxy for the {@link FakeTechAdvance} class.
 *
 * <p>
 * This proxy does not serialize the game data owner to avoid a circular reference. Instances of {@link FakeTechAdvance}
 * created from this proxy will always have their game data set to {@code null}. Proxies that may compose instances of
 * this proxy are required to manually restore the game data in their {@code readResolve()} method via a
 * context-dependent mechanism.
 * </p>
 */
@Immutable
public final class FakeTechAdvanceProxy implements Proxy {
  private static final long serialVersionUID = 8486465678813843350L;

  public static final ProxyFactory FACTORY = ProxyFactory.newInstance(FakeTechAdvance.class, FakeTechAdvanceProxy::new);

  private final Map<String, IAttachment> attachments;
  private final String name;

  public FakeTechAdvanceProxy(final FakeTechAdvance fakeTechAdvance) {
    checkNotNull(fakeTechAdvance);

    attachments = fakeTechAdvance.getAttachments();
    name = fakeTechAdvance.getName();
  }

  @Override
  public Object readResolve() {
    final FakeTechAdvance fakeTechAdvance = new FakeTechAdvance(null, name);
    attachments.forEach(fakeTechAdvance::addAttachment);
    return fakeTechAdvance;
  }
}
