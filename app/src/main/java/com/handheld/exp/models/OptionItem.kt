package com.handheld.exp.models

class OptionItem(
    label: String,
    key: String,
    var options: List<Option>,
    var onOptionChange: (option: Option) -> Unit
) : Item(label, key) {

    private var selectedOptionIndex = 0

    fun setOption(optionKey: String){
        val index = options.indexOfFirst { it.key == optionKey }

        if(index < 0){
            selectedOptionIndex = 0;
        }

        if(selectedOptionIndex == index){
            return
        }

        selectedOptionIndex = index
    }

    fun nextOption(){
        selectedOptionIndex++;
        if(selectedOptionIndex >= options.size){
            selectedOptionIndex = 0;
        }
    }

    fun prevOption(){
        selectedOptionIndex--;
        if(selectedOptionIndex < 0){
            selectedOptionIndex = options.size - 1;
        }
    }

    fun getOption(): Option {
        return options[selectedOptionIndex]
    }

    fun notifyOnOptionChange(){
        onOptionChange(getOption())
    }
}