#include <iostream>

int main() {
    // Declare variables
    int num1, num2, sum, difference, product;
    double quotient;

    // Ask the user to enter two numbers
    std::cout << "Enter the first number: ";
    std::cin >> num1;
    std::cout << "Enter the second number: ";
    std::cin >> num2;

    // Perform arithmetic operations
    sum = num1 + num2;
    difference = num1 - num2;
    product = num1 * num2;

    // Check for division by zero
    if (num2 != 0) {
        quotient = static_cast<double>(num1) / num2;
    } else {
        std::cout << "Error: Division by zero is not allowed." << std::endl;
        return 1; // Exit the program with an error code
    }

    // Display the results
    std::cout << "Sum: " << sum << std::endl;
    std::cout << "Difference: " << difference << std::endl;
    std::cout << "Product: " << product << std::endl;
    std::cout << "Quotient: " << quotient << std::endl;

    return 0; // Exit the program successfully
}
