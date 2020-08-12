package net.datadeer.common;
/*
main Observer code


ArrayList<Observer> listeners = new ArrayList<>();
@Override public void addListener(Observer o) {listeners.add(o);}
@Override public void removeListener(Observer o) {listeners.remove(o);}
@Override public void update() {for(Observer o : listeners) o.observe(this);}


*/
public interface Observable {
	void addListener(Observer o);
	void removeListener(Observer o);
	void update();
}
