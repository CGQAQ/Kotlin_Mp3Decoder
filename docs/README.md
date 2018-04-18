# Mp3 data structure

## Audio Sequence
```text
audio sequence()
{
    while (true)
    {
        frame()
    }  
}
```

## Audio Frame
```text
frame()
{
    header()
    error_check()
    audio_data()
    ancillary_data()
}
```


### <a name="header">Header
```text
header()
{
    syncword            12  bits    
    ID                  1   bits    
    layer               2   bits    
    protection_bit      1   bits    
    bitrate_index       4   bits    
    sampling_frequency  2   bits    
    padding_bit         1   bits    
    private_bit         1   bits    
    mode                2   bits    
    mode_extension      2   bits    
    copyright           1   bits    
    original/home       1   bits    
    emphasis            2   bits    
}
```

## Error check
```text
error_check()
{
    if (protection_bit==0)
    crc_check                   16 bits
    rpchof
}
```

## Ancillary data
```text
if(layer == 1 || layer == 2)
{
    ancillary_data()
    {
        while (nextbits() != syncword)
        {
            ancillary_bit
        }
    }
}
```