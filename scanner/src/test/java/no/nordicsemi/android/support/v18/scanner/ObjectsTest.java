package no.nordicsemi.android.support.v18.scanner;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class ObjectsTest {

  @Test public void toString_nullValueAsParam_returnNullString() {
    // Given
    final Object objectA = null;

    // When
    final String nullString = Objects.toString(objectA);

    // Then
    assertThat(nullString, is(notNullValue()));
    assertThat(nullString, is("null"));
  }

  @Test public void toString_nonNullValueAsParam_returnObjectToStringValue() {
    // Given
    final Object objectA = new Object() {
      @Override public String toString() {
        return "notNull";
      }
    };

    // When
    final String nonNullString = Objects.toString(objectA);

    // Then
    assertThat(nonNullString, is(nonNullString));
    assertThat(nonNullString, is("notNull"));
  }

  @Test public void hash_nullValueAsParam_returnNumber() {
    // Given
    final Object nullObject = null;

    // When
    final int hash = Objects.hash(nullObject);

    // Then
    assertThat(hash, is(notNullValue()));
  }

  @Test public void hash_ObjectValueAsParam_returnNumber() {
    // Given
    final Object objectA = new Object();

    // When
    final int hash = Objects.hash(objectA);

    // Then
    assertThat(hash, is(notNullValue()));
  }

  @Test public void equals_nullValueAsFirstParam_returnFalse() {
    // Given
    final Object objectA = null;
    final Object objectB = new Object();

    // When
    final boolean result = Objects.equals(objectA, objectB);

    // Then
    assertThat(result, is(false));
  }

  @Test public void equals_nullValueAsSecondParam_returnFalse() {
    // Given
    final Object objectA = new Object();
    final Object objectB = null;

    // When
    final boolean result = Objects.equals(objectA, objectB);

    // Then
    assertThat(result, is(false));
  }

  @Test public void equals_nullValueAsBothParams_returnTrue() {
    // Given
    final Object objectA = null;
    final Object objectB = null;

    // When
    final boolean result = Objects.equals(objectA, objectB);

    // Then
    assertThat(result, is(true));
  }

  @Test public void equals_differentBooleanParams_returnFalse() {
    // Given
    final boolean paramA = true;
    final boolean paramB = false;

    // When
    final boolean result = Objects.equals(paramA, paramB);

    // Then
    assertThat(result, is(false));
  }

  @Test public void equals_sameBooleanParams_returnTrue() {
    // Given
    final boolean paramA = true;
    final boolean paramB = true;

    // When
    final boolean result = Objects.equals(paramA, paramB);

    // Then
    assertThat(result, is(true));
  }

}