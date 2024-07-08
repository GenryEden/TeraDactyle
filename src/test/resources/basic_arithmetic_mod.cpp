#include <iostream>

void getInput(int &a, int &b) {
    std::cout << "Enter two integers separated by space: ";
    std::cin >> a >> b;
}

int add(int a, int b) {
    return a + b;
}

int subtract(int a, int b) {
    return a - b;
}

int multiply(int a, int b) {
    return a * b;
}

bool divide(int a, int b, double &result) {
    if (b == 0) {
        return false;
    }
    result = static_cast<double>(a) / b;
    return true;
}

void displayResults(int sum, int difference, int product, double quotient, bool divisionSuccessful) {
    std::cout << "Addition result: " << sum << std::endl;
    std::cout << "Subtraction result: " << difference << std::endl;
    std::cout << "Multiplication result: " << product << std::endl;
    if (divisionSuccessful) {
        std::cout << "Division result: " << quotient << std::endl;
    } else {
        std::cout << "Division by zero is not allowed." << std::endl;
    }
}

int main() {
    int num1, num2;
    getInput(num1, num2);

    int sum = add(num1, num2);
    int difference = subtract(num1, num2);
    int product = multiply(num1, num2);
    double quotient;
    bool divisionSuccessful = divide(num1, num2, quotient);

    displayResults(sum, difference, product, quotient, divisionSuccessful);

    return 0;
}
