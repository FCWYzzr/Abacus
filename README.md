# “算盘”语言用户手册
## 前言
&emsp;&emsp;计算机科学发展到现在已经经历了百余个春秋。作为人类与机器的沟通桥梁，程序设计语言也层出不穷。各个国家也抓紧着对年轻一代的教育，期盼着某些人能从“小白”蜕变成“大佬”。<br/>
&emsp;&emsp;但，我们所走的路，真的是完全正确的吗？<br/>
&emsp;&emsp;如今我国程序设计基础教育的标准语言是Python。尽管Python以简单著称，但事实上更多人是在被动的学或者功利地学。哪怕是偏专业领域的初学者，也难免陷入功利被动的困境。我身边不少人表示“能过就行”，“今后再不碰编程”。此外，就连IT圈内也有种种不良风气：语言歧视，语言崇拜，华而不实，小题大做……<br/>
&emsp;&emsp;编程应当成为与螺丝刀，扳手，智能手机，计算器一样人人都可以轻松使用的下里巴人，而不是自娱自乐孤芳自赏的阳春白雪。<br/>
&emsp;&emsp;一门好的面相非专业者的程序设计语言应该好用而不复杂，专门化而不强大，应该前所未有的符合人的直觉和简便易懂。<br/>
&emsp;&emsp;于是我设计了这门语言，它满足了我所构思的部分特性，我也会逐步完善它，直到它真的成为一门好用的工具。

## Q&A

- __Q__：这门语言好学吗？<br/>
__A__：尽管我已经尽力去设计的符合直觉，但具体体验还是会 __因人而异__。因此我欢迎大家提出宝贵意见，我也会尽力修改地使其更加合理。现在一部分语法非常贴合直觉，比如循环n次可以使用标明一部分语句然后乘上n来处理，尽管牺牲了部分灵活性，但是也使得其更贴近直觉。<br/><br/>

- __Q__：Abacus是什么意思？<br/>
__A__：Abacus是算盘的意思，我希望这门语言可以取代传统的科学计算器，并且推广到全世界。让非专业但有需求者可以拥有一种趁手的工具。<br/><br/>

- __Q__：这门语言会开源 __免费__ 吗？<br/>
__A__：__是的__。基础服务内容比如编译器，解释器，图形化界面以后也会一直保持开源，但拓展服务，比如更专业的处理或者更多的功能比如Excel的接口会考虑收费。<br/><br/>

- __Q__：我可以用这门语言代替其他简单语言（比如Python）吗？<br/>
__A__：__不能__ 。这门语言更倾向于计算方面,适用于批量计算处理的方面，其一般架构为IPO。相比之下其他语言比如Python更为全面。Abacus是一门领域特定语言（DSL）而Python是通用语言（GPL）。<br/><br/>

- __Q__：我已经有其他语言的编程基础，我需要多长时间才能学会这门语言？<br/>
__A__：小于5分钟。<br/><br/>

## 简介：
__算盘__（Abacus）语言是一个对无基础/非专业人士流程化处理数据的解决方案。Abacus的语法设计贴近思维直觉，语法灵活，易于无基础/非专业人士学习。可以说 __会算数学就可以学会算盘__

## 快速入门：

- 新建任意文本文件（建议修改拓展名为aba）。
编辑文件内容为程序代码。
- 使用Abacus.exe运行

- 算盘使用换行判断指令，因此写完语句后请换行。

### 语法结构

- ### 输入命令：
```abacus
input x, y, z …
```

- - 其中xyz可以为变量名称或者变量声明<br>
- - 尚未声明的变量会被认为是字符串，读入一行<br>
- - 已被声明的变量会按照类型读入<br>
重新声明变量会覆盖原声明，这会丢失原数据<br>

- ### 输出命令：
``` abacus
output: x, y, z
```

- - 其中xyz可以为变量名称，字符串，数字，运算式（比如1+1）或者类型转换（比如 int 1.3，输出为1，不会四舍五入）
- - 已被声明的变量会按照类型输出
- - 运算式会先运算再输出，包含类型转换会先运算再转换

- ### 运算：
```abacus
x=y*z+1
```
- - 如果左侧变量没有定义，则会被认为是实数（带小数）保存

- ### 判断：
```abacus
condition ?
Y: ...

N: ...
```
- - condition如果返回不是0，则执行Y，否则执行N
- - 注意Y/N都可以不写，不写不执行

- ### 循环：
```abacus
{
   operation
} * n
```
- - 执行operation操作，重复n次，n可以为inf（关键字）

### 基础类
- ### 整数类型int
```abacus
input int a, int b
output: a+b, a-b, a*b, a/b
```
- - 注意整数间的除法是整除
- - %可以取余数

- ### 实数类型real
```abacus
input real a, real b
output: a+b, a-b, a*b, a/b
```
- - 使用方法与int相同
- - 除法是真正的除法
- - 输出总是保留两位小数

- ### 文本类型str
```abacus
input str a
input b
output: a, b, a*3
```
- - 很多运算都受限
- - 一般仅作为提示出现

### 内置函数
-  sqrt函数计算一个数字的开平方,返回实数
```abacus
output: sqrt(4)
```
-  absolute函数计算一个数字的绝对值,返回实数
```abacus
output: absolute(-4)
```
-  round函数对一个 __实数__ 四舍五入,返回 __整数__
```abacus
output: round(0.5)
```
- pow函数计算 __第一个参数__ __的第二个参数__ 次方,当第一个参数为数字时，第二个参数可以为整数或实数；第一个参数为矩阵时，第二个参数只能为 __非负整数__
```abacus
output: round(5, 3)
```
### 内置工具
- ### 向量
- - 向量可以像数字类型一样被定义，输入和使用，输入的格式是<br>
```console
1 2 3 4 5 ...
```
- - 代码是
```abacus
input: vector a1
```
- - 两个相同维度的向量可以相加/减（或求积，见下）
```abacus
input vector a1, vector a2
output: a1 + a2
output: a1 - a2
```
- - 两个向量可以叉乘（*），也可以点乘（·，也就是中文分隔符）。注意叉乘仅允许二维或三维互相运算，二维的第三位数字默认为0
```abacus
input: vector a1, vector a2

output: a1 * a2
output: a1 · a2
```
- - 向量可以与数字相乘，结果是每一个元素乘上数字的新向量， 左右乘没有区别。
```abacus
input vector a1

output: 2 * a1
output: a1 * 2
```
- - 可以使用内置函数 _norm_ 求其模/范数，不指定 p 时，求2-范数（模）
```abacus
output norm(a1)
output norm(a1,1)
```

- - 向量也可以作为一组数据进行运算，求和，方差和平均数
```abacus
input vector a1

output: summation(a1)
output: average(a1)
output: variance(a1)
```

- ### 矩阵
- - 矩阵可以像数字类型一样被定义，输入和使用，输入的格式是<br>
```console
1 2 3
4 5 6
7 8 9

```
- - 注意最后的空行。代码是
```abacus
input: matrix A
```

- - 两个矩阵可以相乘（*），如果不符合运算规律会报错
```abacus
input: matrix a1, matrix a2

output: a1 * a2
```
- - 两个相同维度矩阵可以相加/减
```abacus
input: matrix a1, matrix a2

output: a1 + a2
output: a1 - a2
```

- - 为了保证行列式的合理性，矩阵不可以与数字相乘。
- - 可以使用build内置函数来通过两个向量构架矩阵
```abacus
input vector a1, vector a2

output: build(a1,a2)
```
- - 可以使用内置函数 _norm_ 求其p-范数，不指定 p 时，求2-范数(尽管有inf关键字，但暂未实现$\infty$-范数)
```abacus
output norm(A)

output norm(A,1)
```
- - 可以使用内置函数Triangle处理矩阵，产生上(U)下(D)三角阵，或者使用Diagonal产生对角阵。注意如果矩阵为奇异矩阵，Diagonal可能会返回0阵
```abacus
input matrix A

output: Triangle(A, "U")
output: Triangle(A, "D")
output: Diagonal(A)
```
- - 可以使用内置函数Determinant处理矩阵，计算行列式
```abacus
input matrix A

output: Determinant(A)
```
- - 可以使用内置函数Minor计算i,j的（代数）余子式
```abacus
input matrix A

output: Minor(A, 1, 2)
output: Minor(A, 1, 2, "normal")
output: Minor(A, 1, 2, "complemental")
```
- - 矩阵不可以除以一个数字，但介于矩阵求逆需要这个操作，我也给出了求逆的内置函数。
- - 可以使用内置函数Adjoint计算矩阵的伴随矩阵，Inverse计算逆（奇异矩阵报错）
```abacus
input matrix A

output: Adjoint(A)
output: Inverse(A)
```
