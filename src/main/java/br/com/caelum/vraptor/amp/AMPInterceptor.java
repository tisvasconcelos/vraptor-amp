package br.com.caelum.vraptor.amp;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import br.com.caelum.vraptor.InterceptionException;
import br.com.caelum.vraptor.Intercepts;
import br.com.caelum.vraptor.Result;
import br.com.caelum.vraptor.controller.ControllerMethod;
import br.com.caelum.vraptor.core.InterceptorStack;
import br.com.caelum.vraptor.interceptor.Interceptor;
import br.com.caelum.vraptor.view.Results;

@Intercepts
@RequestScoped
public class AMPInterceptor implements Interceptor {

	@Inject
	private HttpServletRequest request;
	
	@Inject
	private Result result;
	
	@Override
	public void intercept(InterceptorStack stack, ControllerMethod method, Object controllerInstance) throws InterceptionException {
		String requestURL = request.getRequestURL().toString();
		
		boolean hasQueryString = request.getQueryString() != null;
		String newURL = requestURL + (hasQueryString ? "?" + request.getQueryString() : "");
		
		if(request.getParameter("amp") != null) {
			result.include("ampCanonical", newURL);
			stack.next(method, controllerInstance);
			AMPAvailable annotation = method.getMethod().getAnnotation(AMPAvailable.class);
			result.use(Results.page()).forwardTo(annotation.value());
		} else {
			result.include("ampHTML", newURL + (newURL.indexOf("?") != -1 ? "&" : "?") + "amp=true");
			stack.next(method, controllerInstance);
		}
		
	}

	@Override
	public boolean accepts(ControllerMethod method) {
		return method.containsAnnotation(AMPAvailable.class);
	}
	
}
