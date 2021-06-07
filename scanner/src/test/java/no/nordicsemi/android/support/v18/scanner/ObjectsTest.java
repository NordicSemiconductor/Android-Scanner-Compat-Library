package no.nordicsemi.android.support.v18.scanner;

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class ObjectsTest {

  @Test public void toString_nullValueAsParam_returnNullString() {
    // Given
    final Object objectA = null;

    // When
    final String nullString = Objects.toString(objectA);

    // Then
    assertThat(nullString).isEqualTo("null");
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
    assertThat(nonNullString).isEqualTo("notNull");
  }

  @Test public void equals_nullValueAsFirstParam_returnFalse() {
    // Given
    final Object objectA = null;
    final Object objectB = new Object();

    // When
    //noinspection ConstantConditions
    final boolean result = Objects.equals(objectA, objectB);

    // Then
    assertThat(result).isFalse();
  }

  @Test public void equals_nullValueAsSecondParam_returnFalse() {
    // Given
    final Object objectA = new Object();
    final Object objectB = null;

    // When
    final boolean result = Objects.equals(objectA, objectB);

    // Then
    assertThat(result).isFalse();
  }

  @Test public void equals_nullValueAsBothParams_returnTrue() {
    // Given
    final Object objectA = null;
    final Object objectB = null;

    // When
    //noinspection ConstantConditions
    final boolean result = Objects.equals(objectA, objectB);

    // Then
    assertThat(result).isTrue();
  }

  @Test public void equals_differentBooleanParams_returnFalse() {
    // Given
    final boolean paramA = true;
    final boolean paramB = false;

    // When
    final boolean result = Objects.equals(paramA, paramB);

    // Then
    assertThat(result).isFalse();
  }

  @Test public void equals_sameBooleanParams_returnTrue() {
    // Given
    final boolean paramA = true;
    final boolean paramB = true;

    // When
    final boolean result = Objects.equals(paramA, paramB);

    // Then
    assertThat(result).isTrue();
  }

}