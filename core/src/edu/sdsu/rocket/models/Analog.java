package edu.sdsu.rocket.models;

import edu.sdsu.rocket.helpers.AtomicFloat;

public class Analog {

	private AtomicFloat a0 = new AtomicFloat();
	private AtomicFloat a1 = new AtomicFloat();
	private AtomicFloat a2 = new AtomicFloat();
	private AtomicFloat a3 = new AtomicFloat();
	
	public void setA0(float value) {
		a0.set(value);
	}
	
	public float getA0() {
		return a0.get();
	}
	
	public void setA1(float value) {
		a1.set(value);
	}
	
	public float getA1() {
		return a1.get();
	}
	
	public void setA2(float value) {
		a2.set(value);
	}
	
	public float getA2() {
		return a2.get();
	}
	
	public void setA3(float value) {
		a3.set(value);
	}
	
	public float getA3() {
		return a3.get();
	}
	
	public void set(int index, float value) {
		switch (index) {
		case 0:
			setA0(value);
			break;
		case 1: 
			setA1(value);
			break;
		case 2: 
			setA2(value);
			break;
		case 3: 
			setA3(value);
			break;
		default:
			throw new IndexOutOfBoundsException("Invalid analog index: " + index);
		}
	}
	
	public float get(int index) {
		switch (index) {
		case 0: return getA0();
		case 1: return getA1();
		case 2: return getA2();
		case 3: return getA3();
		default:
			throw new IndexOutOfBoundsException("Invalid analog index: " + index);
		}
	}
	
	@Override
	public String toString() {
		return super.toString()
				+ ": A0=" + getA0()
				+ ", A1=" + getA1()
				+ ", A2=" + getA2()
				+ ", A3=" + getA3();
	}
	
}
